package com.safezone.data.repository;

import com.safezone.data.db.VaultFileDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class VaultRepository_Factory implements Factory<VaultRepository> {
  private final Provider<VaultFileDao> daoProvider;

  public VaultRepository_Factory(Provider<VaultFileDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public VaultRepository get() {
    return newInstance(daoProvider.get());
  }

  public static VaultRepository_Factory create(Provider<VaultFileDao> daoProvider) {
    return new VaultRepository_Factory(daoProvider);
  }

  public static VaultRepository newInstance(VaultFileDao dao) {
    return new VaultRepository(dao);
  }
}
