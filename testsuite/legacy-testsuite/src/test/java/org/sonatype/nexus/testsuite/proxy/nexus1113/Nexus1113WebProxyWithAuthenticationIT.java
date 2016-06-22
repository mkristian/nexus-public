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
package org.sonatype.nexus.testsuite.proxy.nexus1113;

import java.io.File;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.sonatype.nexus.integrationtests.ITGroups.PROXY;
import org.sonatype.nexus.test.http.HttpProxyServer;
import org.sonatype.nexus.test.http.HttpProxyServer.RequestResponseListener;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.testsuite.proxy.AbstractNexusWebProxyIntegrationTest;

import com.google.common.collect.ImmutableMap;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Request;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class Nexus1113WebProxyWithAuthenticationIT
    extends AbstractNexusWebProxyIntegrationTest
{

  @Override
  @Before
  public void startWebProxy()
      throws Exception
  {
    httpProxyServer = new HttpProxyServer(
        webProxyPort,
        new RequestResponseListener()
        {
          @Override
          public void servicing(final ServletRequest req, final ServletResponse res) {
            final HttpURI uri = ((Request) req).getHttpURI();
            accessedUris.add(uri.toString());
          }
        },
        ImmutableMap.of("admin", "123")
    );
    httpProxyServer.start();
  }

  @Test
  @Category(PROXY.class)
  public void downloadArtifactOverWebProxy()
      throws Exception
  {
    File pomFile = this.getLocalFile("release-proxy-repo-1", "nexus1113", "artifact", "1.0", "pom");
    File pomArtifact =
        this.downloadArtifact("nexus1113", "artifact", "1.0", "pom", null, "target/downloads/nexus1113");
    Assert.assertTrue(FileTestingUtils.compareFileSHA1s(pomArtifact, pomFile));

    File jarFile = this.getLocalFile("release-proxy-repo-1", "nexus1113", "artifact", "1.0", "jar");
    File jarArtifact =
        this.downloadArtifact("nexus1113", "artifact", "1.0", "jar", null, "target/downloads/nexus1113");
    Assert.assertTrue(FileTestingUtils.compareFileSHA1s(jarArtifact, jarFile));

    String artifactUrl = baseProxyURL + "release-proxy-repo-1/nexus1113/artifact/1.0/artifact-1.0.jar";
    Assert.assertTrue("Proxy was not accessed", accessedUris.contains(artifactUrl));
  }
}
