package com.safezone.di;

import com.safezone.data.db.VaultFileDao;
import com.safezone.data.repository.VaultRepository;
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
public final class AppModule_ProvideVaultRepositoryFactory implements Factory<VaultRepository> {
  private final Provider<VaultFileDao> daoProvider;

  public AppModule_ProvideVaultRepositoryFactory(Provider<VaultFileDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public VaultRepository get() {
    return provideVaultRepository(daoProvider.get());
  }

  public static AppModule_ProvideVaultRepositoryFactory create(Provider<VaultFileDao> daoProvider) {
    return new AppModule_ProvideVaultRepositoryFactory(daoProvider);
  }

  public static VaultRepository provideVaultRepository(VaultFileDao dao) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideVaultRepository(dao));
  }
}
