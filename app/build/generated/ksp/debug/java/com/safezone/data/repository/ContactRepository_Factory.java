package com.safezone.data.repository;

import com.safezone.data.db.ContactDao;
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
public final class ContactRepository_Factory implements Factory<ContactRepository> {
  private final Provider<ContactDao> daoProvider;

  public ContactRepository_Factory(Provider<ContactDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public ContactRepository get() {
    return newInstance(daoProvider.get());
  }

  public static ContactRepository_Factory create(Provider<ContactDao> daoProvider) {
    return new ContactRepository_Factory(daoProvider);
  }

  public static ContactRepository newInstance(ContactDao dao) {
    return new ContactRepository(dao);
  }
}
