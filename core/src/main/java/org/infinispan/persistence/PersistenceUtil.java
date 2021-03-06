package org.infinispan.persistence;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.infinispan.container.DataContainer;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.container.entries.InternalCacheValue;
import org.infinispan.container.impl.InternalEntryFactory;
import org.infinispan.context.InvocationContext;
import org.infinispan.filter.KeyFilter;
import org.infinispan.marshall.core.MarshalledEntry;
import org.infinispan.metadata.InternalMetadata;
import org.infinispan.metadata.impl.InternalMetadataImpl;
import org.infinispan.persistence.manager.PersistenceManager;
import org.infinispan.persistence.spi.AdvancedCacheLoader;
import org.infinispan.util.TimeService;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

import io.reactivex.Flowable;

/**
 * @author Mircea Markus
 * @since 6.0
 */
public class PersistenceUtil {

   private static Log log = LogFactory.getLog(PersistenceUtil.class);
   private static final boolean trace = log.isTraceEnabled();

   public static KeyFilter notNull(KeyFilter filter) {
      return filter == null ? KeyFilter.ACCEPT_ALL_FILTER : filter;
   }

   private static final int SEGMENT_NOT_PROVIDED = -1;

   /**
    *
    * @param acl
    * @param filter
    * @param <K>
    * @param <V>
    * @return
    * @deprecated Please use {@link #count(AdvancedCacheLoader, Predicate)} instead
    */
   @Deprecated
   public static <K, V> int count(AdvancedCacheLoader<K, V> acl, KeyFilter<? super K> filter) {
      return count(acl, (Predicate<? super K>) filter);
   }

   public static <K, V> int count(AdvancedCacheLoader<K, V> acl, Predicate<? super K> filter) {

      // This can't be null
      Long result = Flowable.fromPublisher(acl.publishKeys(filter)).count().blockingGet();
      if (result > Integer.MAX_VALUE) {
         return Integer.MAX_VALUE;
      }
      return result.intValue();
   }

   /**
    *
    * @param acl
    * @param filter
    * @param <K>
    * @param <V>
    * @return
    * @deprecated Please use {@link #toKeySet(AdvancedCacheLoader, Predicate)} instead
    */
   @Deprecated
   public static <K, V> Set<K> toKeySet(AdvancedCacheLoader<K, V> acl, KeyFilter<? super K> filter) {
      return toKeySet(acl, (Predicate<? super K>) filter);
   }

   public static <K, V> Set<K> toKeySet(AdvancedCacheLoader<K, V> acl, Predicate<? super K> filter) {
      if (acl == null)
         return Collections.emptySet();
      return Flowable.fromPublisher(acl.publishKeys(filter))
            .collectInto(new HashSet<K>(), Set::add).blockingGet();
   }

   /**
    *
    * @param acl
    * @param filter
    * @param ief
    * @param <K>
    * @param <V>
    * @return
    * @deprecated Please use {@link #toEntrySet(AdvancedCacheLoader, Predicate, InternalEntryFactory)} instead
    */
   @Deprecated
   public static <K, V> Set<InternalCacheEntry> toEntrySet(AdvancedCacheLoader<K, V> acl, KeyFilter<? super K> filter, final InternalEntryFactory ief) {
      Set entrySet = toEntrySet(acl, (Predicate<? super K>) filter, ief);
      return (Set<InternalCacheEntry>) entrySet;
   }

   public static <K, V> Set<InternalCacheEntry<K, V>> toEntrySet(AdvancedCacheLoader<K, V> acl, Predicate<? super K> filter, final InternalEntryFactory ief) {
      if (acl == null)
         return Collections.emptySet();
      return Flowable.fromPublisher(acl.publishEntries(filter, true, true))
            .map(me -> ief.create(me.getKey(), me.getValue(), me.getMetadata()))
            .collectInto(new HashSet<InternalCacheEntry<K, V>>(), Set::add).blockingGet();
   }

   public static long getExpiryTime(InternalMetadata internalMetadata) {
      return internalMetadata == null ? -1 : internalMetadata.expiryTime();
   }

   public static InternalMetadata internalMetadata(InternalCacheEntry ice) {
      return ice.getMetadata() == null ? null : new InternalMetadataImpl(ice);
   }

   public static InternalMetadata internalMetadata(InternalCacheValue icv) {
      return icv.getMetadata() == null ? null : new InternalMetadataImpl(icv.getMetadata(), icv.getCreated(), icv.getLastUsed());
   }

   /**
    * @deprecated since 9.4 This method references PersistenceManager, which isn't a public class
    */
   @Deprecated
   public static <K, V> InternalCacheEntry<K,V> loadAndStoreInDataContainer(DataContainer<K, V> dataContainer,
         final PersistenceManager persistenceManager, K key, final InvocationContext ctx, final TimeService timeService,
         final AtomicReference<Boolean> isLoaded) {
      return org.infinispan.persistence.internal.PersistenceUtil.loadAndStoreInDataContainer(dataContainer,
            persistenceManager, key, ctx, timeService, isLoaded);
   }

   /**
    * @deprecated since 9.4 This method references PersistenceManager, which isn't a public class
    */
   @Deprecated
   public static <K, V> InternalCacheEntry<K,V> loadAndStoreInDataContainer(DataContainer<K, V> dataContainer, int segment,
         final PersistenceManager persistenceManager, K key, final InvocationContext ctx, final TimeService timeService,
                                                         final AtomicReference<Boolean> isLoaded) {
      return org.infinispan.persistence.internal.PersistenceUtil.loadAndStoreInDataContainer(dataContainer, segment,
            persistenceManager, key, ctx, timeService, isLoaded);
   }

   /**
    * @deprecated since 9.4 This method references PersistenceManager, which isn't a public class
    */
   @Deprecated
   public static <K, V> InternalCacheEntry<K,V> loadAndComputeInDataContainer(DataContainer<K, V> dataContainer,
         int segment, final PersistenceManager persistenceManager, K key, final InvocationContext ctx,
         final TimeService timeService, DataContainer.ComputeAction<K, V> action) {
      return org.infinispan.persistence.internal.PersistenceUtil.loadAndComputeInDataContainer(dataContainer, segment,
            persistenceManager, key, ctx, timeService, action);
   }

   /**
    * @deprecated since 9.4 This method references PersistenceManager, which isn't a public class
    */
   @Deprecated
   public static <K, V> MarshalledEntry<K, V> loadAndCheckExpiration(PersistenceManager persistenceManager, Object key,
                                                        InvocationContext context, TimeService timeService) {
      return org.infinispan.persistence.internal.PersistenceUtil.loadAndCheckExpiration(persistenceManager, key,
            SEGMENT_NOT_PROVIDED, context);
   }

   public static <K, V> InternalCacheEntry<K, V> convert(MarshalledEntry<K, V> loaded, InternalEntryFactory factory) {
      return org.infinispan.persistence.internal.PersistenceUtil.convert(loaded, factory);
   }
}
