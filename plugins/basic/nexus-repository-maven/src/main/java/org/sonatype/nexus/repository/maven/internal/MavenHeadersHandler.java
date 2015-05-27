/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.maven.internal;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.PayloadResponse;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.net.HttpHeaders;
import org.apache.http.client.utils.DateUtils;
import org.joda.time.DateTime;

/**
 * Maven headers handler.
 *
 * @since 3.0
 */
@Singleton
@Named
public class MavenHeadersHandler
    extends ComponentSupport
    implements Handler
{
  @Nonnull
  @Override
  public Response handle(final @Nonnull Context context) throws Exception {
    final Response response = context.proceed();
    if (response.getStatus().isSuccessful() && response instanceof PayloadResponse) {
      final Payload payload = ((PayloadResponse) response).getPayload();
      if (payload instanceof Content) {
        final Content content = (Content) payload;
        final DateTime lastModified = content.getAttributes().get(Content.CONTENT_LAST_MODIFIED, DateTime.class);
        if (lastModified != null) {
          response.getHeaders().set(HttpHeaders.LAST_MODIFIED, DateUtils.formatDate(lastModified.toDate()));
        }
        if (response.getStatus().getCode() == HttpStatus.OK) {
          final String etag = content.getAttributes().get(Content.CONTENT_ETAG, String.class);
          if (etag != null) {
            response.getHeaders().set(HttpHeaders.ETAG, "\"" + etag + "\"");
          }
        }
      }
    }
    return response;
  }
}
