package com.safezone;

import com.safezone.data.preferences.SessionManager;
import com.safezone.data.repository.ContactRepository;
import com.safezone.data.repository.SecurityRepository;
import com.safezone.data.repository.VaultRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class SecurityService_MembersInjector implements MembersInjector<SecurityService> {
  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<ContactRepository> contactRepositoryProvider;

  private final Provider<SecurityRepository> securityRepositoryProvider;

  private final Provider<VaultRepository> vaultRepositoryProvider;

  public SecurityService_MembersInjector(Provider<SessionManager> sessionManagerProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<SecurityRepository> securityRepositoryProvider,
      Provider<VaultRepository> vaultRepositoryProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.contactRepositoryProvider = contactRepositoryProvider;
    this.securityRepositoryProvider = securityRepositoryProvider;
    this.vaultRepositoryProvider = vaultRepositoryProvider;
  }

  public static MembersInjector<SecurityService> create(
      Provider<SessionManager> sessionManagerProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<SecurityRepository> securityRepositoryProvider,
      Provider<VaultRepository> vaultRepositoryProvider) {
    return new SecurityService_MembersInjector(sessionManagerProvider, contactRepositoryProvider, securityRepositoryProvider, vaultRepositoryProvider);
  }

  @Override
  public void injectMembers(SecurityService instance) {
    injectSessionManager(instance, sessionManagerProvider.get());
    injectContactRepository(instance, contactRepositoryProvider.get());
    injectSecurityRepository(instance, securityRepositoryProvider.get());
    injectVaultRepository(instance, vaultRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.safezone.SecurityService.sessionManager")
  public static void injectSessionManager(SecurityService instance, SessionManager sessionManager) {
    instance.sessionManager = sessionManager;
  }

  @InjectedFieldSignature("com.safezone.SecurityService.contactRepository")
  public static void injectContactRepository(SecurityService instance,
      ContactRepository contactRepository) {
    instance.contactRepository = contactRepository;
  }

  @InjectedFieldSignature("com.safezone.SecurityService.securityRepository")
  public static void injectSecurityRepository(SecurityService instance,
      SecurityRepository securityRepository) {
    instance.securityRepository = securityRepository;
  }

  @InjectedFieldSignature("com.safezone.SecurityService.vaultRepository")
  public static void injectVaultRepository(SecurityService instance,
      VaultRepository vaultRepository) {
    instance.vaultRepository = vaultRepository;
  }
}
