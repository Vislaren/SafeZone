package com.safezone.di;

import com.safezone.data.db.SafeZoneDatabase;
import com.safezone.data.db.TriggerPhraseDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class AppModule_ProvideTriggerPhraseDaoFactory implements Factory<TriggerPhraseDao> {
  private final Provider<SafeZoneDatabase> dbProvider;

  public AppModule_ProvideTriggerPhraseDaoFactory(Provider<SafeZoneDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public TriggerPhraseDao get() {
    return provideTriggerPhraseDao(dbProvider.get());
  }

  public static AppModule_ProvideTriggerPhraseDaoFactory create(
      Provider<SafeZoneDatabase> dbProvider) {
    return new AppModule_ProvideTriggerPhraseDaoFactory(dbProvider);
  }

  public static TriggerPhraseDao provideTriggerPhraseDao(SafeZoneDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTriggerPhraseDao(db));
  }
}
