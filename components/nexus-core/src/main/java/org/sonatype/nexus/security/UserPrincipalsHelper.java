/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.security;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.security.usermanagement.NoSuchUserManagerException;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserNotFoundTransientException;
import org.sonatype.security.usermanagement.UserStatus;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.apache.shiro.subject.PrincipalCollection;

/**
 * Helper component to map user principals to associated information.
 */
@Named
@Singleton
public class UserPrincipalsHelper
  extends ComponentSupport
{
  @Inject
  private List<UserManager> userManagers;

  /**
   * Searches records to find the status of the user associated with the given principals.
   *
   * @param principals Identifying principals
   * @return User status
   */
  public UserStatus getUserStatus(final PrincipalCollection principals)
      throws UserNotFoundException
  {
    String userId = null;
    if (principals != null) {
      userId = principals.getPrimaryPrincipal().toString();
      try {
        final User user = findUserManager(principals).getUser(userId);
        if (user != null) {
          return user.getStatus();
        }
      }
      catch (final NoSuchUserManagerException e) {
        throw new UserNotFoundException(userId, e.getMessage(), e);
      }
      catch (final UserNotFoundTransientException e) {
        log.debug("Ignoring transient user error: {}", e);
        return UserStatus.disabled;
      }
      catch (final UserNotFoundException e) {
        throw e; // pass back original cause unchanged
      }
      catch (final RuntimeException e) {
        log.debug("Ignoring transient user error: {}", e);
        return UserStatus.disabled;
      }
    }
    throw new UserNotFoundException(userId);
  }

  /**
   * Searches for the {@link UserManager} associated with the given principals.
   *
   * @param principals Identifying principals
   * @return UserManager component
   */
  public UserManager findUserManager(final PrincipalCollection principals)
      throws NoSuchUserManagerException
  {
    if (principals == null) {
      throw new NoSuchUserManagerException("Missing principals");
    }
    boolean isPrimary = true;
    for (String realmName : principals.getRealmNames()) {
      // include secondary realms in the search as long as they have the same userId as the primary
      if (isPrimary || principals.fromRealm(realmName).contains(principals.getPrimaryPrincipal())) {
        for (final UserManager userManager : userManagers) {
          if (realmName.equals(userManager.getAuthenticationRealmName())) {
            return userManager;
          }
          isPrimary = false;
        }
      }
    }
    throw new NoSuchUserManagerException("No UserManager for realm(s): " + principals.getRealmNames());
  }
}
