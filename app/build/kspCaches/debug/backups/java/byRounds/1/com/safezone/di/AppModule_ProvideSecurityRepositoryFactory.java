package com.safezone.di;

import android.content.Context;
import com.safezone.data.db.TriggerPhraseDao;
import com.safezone.data.repository.SecurityRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppModule_ProvideSecurityRepositoryFactory implements Factory<SecurityRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<TriggerPhraseDao> daoProvider;

  public AppModule_ProvideSecurityRepositoryFactory(Provider<Context> contextProvider,
      Provider<TriggerPhraseDao> daoProvider) {
    this.contextProvider = contextProvider;
    this.daoProvider = daoProvider;
  }

  @Override
  public SecurityRepository get() {
    return provideSecurityRepository(contextProvider.get(), daoProvider.get());
  }

  public static AppModule_ProvideSecurityRepositoryFactory create(Provider<Context> contextProvider,
      Provider<TriggerPhraseDao> daoProvider) {
    return new AppModule_ProvideSecurityRepositoryFactory(contextProvider, daoProvider);
  }

  public static SecurityRepository provideSecurityRepository(Context context,
      TriggerPhraseDao dao) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSecurityRepository(context, dao));
  }
}
