package net.zeronexus.quickstackcraft.compat;

import java.lang.reflect.Method;

/**
 * Reads optional client-side slot protection without linking ItemLocks at compile time.
 */
public final class ExternalSlotLocks {

    private static final String COMPONENT_REGISTRY = "com.kirdow.itemlocks.proxy.Components";
    private static final String LOCK_SERVICE = "com.kirdow.itemlocks.client.LockManager";
    private static final long RENDER_CACHE_NANOS = 50_000_000L;

    private record ReflectionApi(Object service, Method rawSlotQuery) {
        boolean locked(int rawSlot) throws ReflectiveOperationException {
            return Boolean.TRUE.equals(rawSlotQuery.invoke(service, rawSlot));
        }
    }

    private static ReflectionApi api;
    private static boolean permanentlyUnavailable;
    private static Boolean detected;
    private static long cachedMask;
    private static long cacheDeadline;

    private ExternalSlotLocks() {}

    public static boolean detected() {
        if (detected == null) {
            try {
                ClassLoader loader = ExternalSlotLocks.class.getClassLoader();
                Class<?> registryType = Class.forName(COMPONENT_REGISTRY, false, loader);
                Class<?> serviceType = Class.forName(LOCK_SERVICE, false, loader);
                registryType.getMethod("getComponent", Class.class);
                serviceType.getMethod("isLockedSlotRaw", int.class);
                detected = true;
            } catch (ReflectiveOperationException | LinkageError | SecurityException ignored) {
                detected = false;
            }
        }
        return detected;
    }

    public static long snapshot() {
        if (!detected() || permanentlyUnavailable) {
            return 0L;
        }

        try {
            ReflectionApi resolved = resolveApi();
            long inventoryBits = 0L;
            for (int rawSlot = 0; rawSlot < SlotMask.INVENTORY_SLOT_COUNT; rawSlot++) {
                if (resolved.locked(rawSlot)) {
                    inventoryBits |= 1L << inventoryIndex(rawSlot);
                }
            }
            return inventoryBits;
        } catch (ClassNotFoundException | NoSuchMethodException | LinkageError incompatibleApi) {
            permanentlyUnavailable = true;
            api = null;
            detected = false;
        } catch (ReflectiveOperationException | RuntimeException notReady) {
            api = null;
        }
        return 0L;
    }

    public static long renderSnapshot() {
        long now = System.nanoTime();
        if (now >= cacheDeadline) {
            cachedMask = snapshot();
            cacheDeadline = now + RENDER_CACHE_NANOS;
        }
        return cachedMask;
    }

    private static ReflectionApi resolveApi() throws ReflectiveOperationException {
        if (api != null) {
            return api;
        }

        ClassLoader loader = ExternalSlotLocks.class.getClassLoader();
        Class<?> registryType = Class.forName(COMPONENT_REGISTRY, false, loader);
        Class<?> serviceType = Class.forName(LOCK_SERVICE, false, loader);
        Method componentLookup = registryType.getMethod("getComponent", Class.class);
        Object service = componentLookup.invoke(null, serviceType);
        if (service == null) {
            throw new IllegalStateException("ItemLocks service is not initialized");
        }

        api = new ReflectionApi(service, serviceType.getMethod("isLockedSlotRaw", int.class));
        return api;
    }

    private static int inventoryIndex(int rawSlot) {
        return rawSlot < 27 ? rawSlot + 9 : rawSlot - 27;
    }
}
