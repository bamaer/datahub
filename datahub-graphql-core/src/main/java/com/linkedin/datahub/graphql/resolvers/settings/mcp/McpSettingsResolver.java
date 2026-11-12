package com.linkedin.datahub.graphql.resolvers.settings.mcp;

import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.concurrency.GraphQLConcurrencyUtils;
import com.linkedin.datahub.graphql.generated.McpSettings;
import com.linkedin.metadata.service.SettingsService;
import com.linkedin.settings.global.GlobalSettingsInfo;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Retrieves the global MCP (Model Context Protocol) server settings.
 *
 * <p>Readable by any authenticated user, like globalViewsSettings.
 */
public class McpSettingsResolver implements DataFetcher<CompletableFuture<McpSettings>> {

  private final SettingsService _settingsService;

  public McpSettingsResolver(final SettingsService settingsService) {
    _settingsService = Objects.requireNonNull(settingsService, "settingsService must not be null");
  }

  @Override
  public CompletableFuture<McpSettings> get(final DataFetchingEnvironment environment)
      throws Exception {
    final QueryContext context = environment.getContext();
    return GraphQLConcurrencyUtils.supplyAsync(
        () -> {
          try {
            final GlobalSettingsInfo globalSettings =
                _settingsService.getGlobalSettings(context.getOperationContext());
            return McpSettingsMapper.map(
                globalSettings != null && globalSettings.hasMcpSettings()
                    ? globalSettings.getMcpSettings()
                    : null);
          } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve MCP settings", e);
          }
        },
        this.getClass().getSimpleName(),
        "get");
  }
}
