package com.safezone.di;

import com.safezone.data.db.SafeZoneDatabase;
import com.safezone.data.db.VaultFileDao;
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
public final class AppModule_ProvideVaultFileDaoFactory implements Factory<VaultFileDao> {
  private final Provider<SafeZoneDatabase> dbProvider;

  public AppModule_ProvideVaultFileDaoFactory(Provider<SafeZoneDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public VaultFileDao get() {
    return provideVaultFileDao(dbProvider.get());
  }

  public static AppModule_ProvideVaultFileDaoFactory create(Provider<SafeZoneDatabase> dbProvider) {
    return new AppModule_ProvideVaultFileDaoFactory(dbProvider);
  }

  public static VaultFileDao provideVaultFileDao(SafeZoneDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideVaultFileDao(db));
  }
}
