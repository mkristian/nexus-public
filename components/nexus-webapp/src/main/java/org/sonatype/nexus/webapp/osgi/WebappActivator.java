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
package org.sonatype.nexus.webapp.osgi;

import org.sonatype.nexus.NxApplication;
import org.sonatype.nexus.bootstrap.ConfigurationHolder;
import org.sonatype.nexus.guice.NexusModules.CoreModule;
import org.sonatype.nexus.log.LogManager;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.eclipse.sisu.plexus.PlexusSpaceModule;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.WireModule;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web application activator.
 *
 * @since 2.14
 */
public class WebappActivator
    implements BundleActivator
{
  private static final Logger log = LoggerFactory.getLogger(WebappActivator.class);

  private Injector injector;

  private PlexusContainer container;

  private NxApplication application;

  private LogManager logManager;

  @Override
  public void start(final BundleContext context) throws Exception {
    // create the injector
    ClassSpace coreSpace = new URLClassSpace(Thread.currentThread().getContextClassLoader());
    injector = Guice.createInjector(
        new WireModule(
            new CoreModule(context, ConfigurationHolder.get(), context.getBundle()),
            new PlexusSpaceModule(coreSpace, BeanScanning.INDEX)));
    log.debug("Injector: {}", injector);

    container = injector.getInstance(PlexusContainer.class);
    context.setAttribute(PlexusConstants.PLEXUS_KEY, container);
    injector.getInstance(Context.class).put(PlexusConstants.PLEXUS_KEY, container);
    log.debug("Container: {}", container);

    // configure logging
    logManager = container.lookup(LogManager.class);
    log.debug("Log manager: {}", logManager);
    logManager.configure();

    // start the application
    application = container.lookup(NxApplication.class);
    log.debug("Application: {}", application);
    application.start();
  }

  @Override
  public void stop(final BundleContext context) throws Exception {
    // stop application
    if (application != null) {
      try {
        application.stop();
      }
      catch (Exception e) {
        log.error("Failed to stop application", e);
      }
      application = null;
    }

    // shutdown logging
    if (logManager != null) {
      logManager.shutdown();
      logManager = null;
    }

    injector = null;

    // cleanup the container
    if (container != null) {
      container.dispose();
      // context.removeAttribute(PlexusConstants.PLEXUS_KEY);
      container = null;
    }
  }
}
