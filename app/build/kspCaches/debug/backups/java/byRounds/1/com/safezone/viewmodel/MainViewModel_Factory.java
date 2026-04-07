package com.safezone.viewmodel;

import android.app.Application;
import com.safezone.data.preferences.SessionManager;
import com.safezone.data.repository.ContactRepository;
import com.safezone.data.repository.SecurityRepository;
import com.safezone.data.repository.VaultRepository;
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
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<Application> applicationProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<ContactRepository> contactRepositoryProvider;

  private final Provider<SecurityRepository> securityRepositoryProvider;

  private final Provider<VaultRepository> vaultRepositoryProvider;

  public MainViewModel_Factory(Provider<Application> applicationProvider,
      Provider<SessionManager> sessionManagerProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<SecurityRepository> securityRepositoryProvider,
      Provider<VaultRepository> vaultRepositoryProvider) {
    this.applicationProvider = applicationProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.contactRepositoryProvider = contactRepositoryProvider;
    this.securityRepositoryProvider = securityRepositoryProvider;
    this.vaultRepositoryProvider = vaultRepositoryProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(applicationProvider.get(), sessionManagerProvider.get(), contactRepositoryProvider.get(), securityRepositoryProvider.get(), vaultRepositoryProvider.get());
  }

  public static MainViewModel_Factory create(Provider<Application> applicationProvider,
      Provider<SessionManager> sessionManagerProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<SecurityRepository> securityRepositoryProvider,
      Provider<VaultRepository> vaultRepositoryProvider) {
    return new MainViewModel_Factory(applicationProvider, sessionManagerProvider, contactRepositoryProvider, securityRepositoryProvider, vaultRepositoryProvider);
  }

  public static MainViewModel newInstance(Application application, SessionManager sessionManager,
      ContactRepository contactRepository, SecurityRepository securityRepository,
      VaultRepository vaultRepository) {
    return new MainViewModel(application, sessionManager, contactRepository, securityRepository, vaultRepository);
  }
}
