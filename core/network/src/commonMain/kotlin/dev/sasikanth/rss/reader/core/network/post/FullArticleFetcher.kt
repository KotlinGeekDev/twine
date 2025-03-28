/*
 * Copyright 2024 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sasikanth.rss.reader.core.network.post

import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class FullArticleFetcher(
  private val httpClient: HttpClient,
  private val dispatchersProvider: DispatchersProvider
) {

  suspend fun fetch(link: String): Result<String> {
    return withContext(dispatchersProvider.io) {
      try {
        val response = httpClient.config { followRedirects = true }.get(link)

        if (
          response.status == HttpStatusCode.OK &&
            response.contentType()?.withoutParameters() == ContentType.Text.Html
        ) {
          val content = response.bodyAsText()
          return@withContext Result.success(content)
        }
      } catch (t: Throwable) {
        // no-op
      }

      return@withContext Result.failure(IllegalArgumentException("Failed to fetch the post"))
    }
  }
}
