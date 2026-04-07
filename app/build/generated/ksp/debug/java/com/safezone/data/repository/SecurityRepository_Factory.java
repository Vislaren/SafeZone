package com.safezone.data.repository;

import android.content.Context;
import com.safezone.data.db.TriggerPhraseDao;
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
public final class SecurityRepository_Factory implements Factory<SecurityRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<TriggerPhraseDao> daoProvider;

  public SecurityRepository_Factory(Provider<Context> contextProvider,
      Provider<TriggerPhraseDao> daoProvider) {
    this.contextProvider = contextProvider;
    this.daoProvider = daoProvider;
  }

  @Override
  public SecurityRepository get() {
    return newInstance(contextProvider.get(), daoProvider.get());
  }

  public static SecurityRepository_Factory create(Provider<Context> contextProvider,
      Provider<TriggerPhraseDao> daoProvider) {
    return new SecurityRepository_Factory(contextProvider, daoProvider);
  }

  public static SecurityRepository newInstance(Context context, TriggerPhraseDao dao) {
    return new SecurityRepository(context, dao);
  }
}
