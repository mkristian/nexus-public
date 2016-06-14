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
package org.sonatype.nexus.repositories.metadata;

import org.sonatype.nexus.NexusAppTestSupport;
import org.sonatype.nexus.proxy.RemoteRepositories;
import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;

import org.junit.Assert;
import org.junit.Test;

public class RemoteMirrorDownloadTest
    extends NexusAppTestSupport
{
  private RemoteRepositories remoteRepositories;

  @Test
  public void testRemoteMetadataDownload() throws Exception {
    NexusRepositoryMetadataHandler repoMetadata = this.lookup(NexusRepositoryMetadataHandler.class);

    String url = remoteRepositories.getUrl("repo-with-mirror");

    RepositoryMetadata metadata = repoMetadata.readRemoteRepositoryMetadata(url);

    Assert.assertNotNull(metadata);
  }

  @Override
  protected void setUp()
      throws Exception
  {
    super.setUp();

    remoteRepositories = RemoteRepositories.builder()
        .repo("repo-with-mirror", "target/test-classes/repo-with-mirror")
        .repo("repo-mirror", "target/test-classes/repo-mirror")
        .build();

    remoteRepositories.start();
  }

  @Override
  protected void tearDown()
      throws Exception
  {
    if (remoteRepositories != null) {
      remoteRepositories.stop();
    }

    super.tearDown();
  }


}
