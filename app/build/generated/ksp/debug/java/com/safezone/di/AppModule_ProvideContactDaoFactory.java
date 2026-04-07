package com.safezone.di;

import com.safezone.data.db.ContactDao;
import com.safezone.data.db.SafeZoneDatabase;
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
public final class AppModule_ProvideContactDaoFactory implements Factory<ContactDao> {
  private final Provider<SafeZoneDatabase> dbProvider;

  public AppModule_ProvideContactDaoFactory(Provider<SafeZoneDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ContactDao get() {
    return provideContactDao(dbProvider.get());
  }

  public static AppModule_ProvideContactDaoFactory create(Provider<SafeZoneDatabase> dbProvider) {
    return new AppModule_ProvideContactDaoFactory(dbProvider);
  }

  public static ContactDao provideContactDao(SafeZoneDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideContactDao(db));
  }
}
