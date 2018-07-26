/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.checks;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ConditionalExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S3699")
public class UseOfEmptyReturnValueCheck extends PHPVisitorCheck {

  private static final Set<String> VOID_FUNCTIONS = new HashSet<>(Arrays.asList(
    // http://php.net/manual/en/function.halt-compiler.php
    "__halt_compiler",
    // http://php.net/manual/en/function.apd-clunk.php
    "apd_clunk",
    // http://php.net/manual/en/function.apd-croak.php
    "apd_croak",
    // http://php.net/manual/en/function.apd-dump-function-table.php
    "apd_dump_function_table",
    // http://php.net/manual/en/function.apd-set-session.php
    "apd_set_session",
    // http://php.net/manual/en/function.apd-set-session-trace.php
    "apd_set_session_trace",
    // http://php.net/manual/en/function.clearstatcache.php
    "clearstatcache",
    // http://php.net/manual/en/function.closedir.php
    "closedir",
    // http://php.net/manual/en/internals2.counter.function.counter-bump.php
    "counter_bump",
    // http://php.net/manual/en/internals2.counter.function.counter-bump-value.php
    "counter_bump_value",
    // http://php.net/manual/en/internals2.counter.function.counter-reset.php
    "counter_reset",
    // http://php.net/manual/en/internals2.counter.function.counter-reset-value.php
    "counter_reset_value",
    // http://php.net/manual/en/function.curl-close.php
    "curl_close",
    // http://php.net/manual/en/function.curl-multi-close.php
    "curl_multi_close",
    // http://php.net/manual/en/function.curl-reset.php
    "curl_reset",
    // http://php.net/manual/en/function.curl-share-close.php
    "curl_share_close",
    // http://php.net/manual/en/function.cyrus-authenticate.php
    "cyrus_authenticate",
    // http://php.net/manual/en/function.dba-close.php
    "dba_close",
    // http://php.net/manual/en/function.debug-print-backtrace.php
    "debug_print_backtrace",
    // http://php.net/manual/en/function.debug-zval-dump.php
    "debug_zval_dump",
    // http://php.net/manual/en/function.define-syslog-variables.php
    "define_syslog_variables",
    // http://php.net/manual/en/function.dio-close.php
    "dio_close",
    // http://php.net/manual/en/function.echo.php
    "echo",
    // http://php.net/manual/en/function.eio-cancel.php
    "eio_cancel",
    // http://php.net/manual/en/function.eio-grp-add.php
    "eio_grp_add",
    // http://php.net/manual/en/function.eio-grp-cancel.php
    "eio_grp_cancel",
    // http://php.net/manual/en/function.eio-grp-limit.php
    "eio_grp_limit",
    // http://php.net/manual/en/function.eio-init.php
    "eio_init",
    // http://php.net/manual/en/function.eio-set-max-idle.php
    "eio_set_max_idle",
    // http://php.net/manual/en/function.eio-set-max-parallel.php
    "eio_set_max_parallel",
    // http://php.net/manual/en/function.eio-set-max-poll-reqs.php
    "eio_set_max_poll_reqs",
    // http://php.net/manual/en/function.eio-set-max-poll-time.php
    "eio_set_max_poll_time",
    // http://php.net/manual/en/function.eio-set-min-parallel.php
    "eio_set_min_parallel",
    // http://php.net/manual/en/function.enchant-dict-add-to-session.php
    "enchant_dict_add_to_session",
    // http://php.net/manual/en/function.error-clear-last.php
    "error_clear_last",
    // http://php.net/manual/en/function.event-base-free.php
    "event_base_free",
    // http://php.net/manual/en/function.event-buffer-fd-set.php
    "event_buffer_fd_set",
    // http://php.net/manual/en/function.event-buffer-free.php
    "event_buffer_free",
    // http://php.net/manual/en/function.event-buffer-timeout-set.php
    "event_buffer_timeout_set",
    // http://php.net/manual/en/function.event-buffer-watermark-set.php
    "event_buffer_watermark_set",
    // http://php.net/manual/en/function.event-free.php
    "event_free",
    // http://php.net/manual/en/eventutil.sslrandpoll.php
    "eventutil::sslrandpoll",
    // http://php.net/manual/en/function.exit.php
    "exit",
    // http://php.net/manual/en/function.fam-close.php
    "fam_close",
    // http://php.net/manual/en/function.fann-print-error.php
    "fann_print_error",
    // http://php.net/manual/en/function.fann-reset-errno.php
    "fann_reset_errno",
    // http://php.net/manual/en/function.fann-reset-errstr.php
    "fann_reset_errstr",
    // http://php.net/manual/en/function.fann-set-error-log.php
    "fann_set_error_log",
    // http://php.net/manual/en/function.fbsql-set-characterset.php
    "fbsql_set_characterset",
    // http://php.net/manual/en/function.fbsql-set-transaction.php
    "fbsql_set_transaction",
    // http://php.net/manual/en/function.fdf-close.php
    "fdf_close",
    // http://php.net/manual/en/function.fdf-header.php
    "fdf_header",
    // http://php.net/manual/en/function.flush.php
    "flush",
    // http://php.net/manual/en/function.gc-disable.php
    "gc_disable",
    // http://php.net/manual/en/function.gc-enable.php
    "gc_enable",
    // http://php.net/manual/en/function.geoip-setup-custom-directory.php
    "geoip_setup_custom_directory",
    // http://php.net/manual/en/function.gnupg-seterrormode.php
    "gnupg_seterrormode",
    // http://php.net/manual/en/function.gupnp-context-set-subscription-timeout.php
    "gupnp_context_set_subscription_timeout",
    // http://php.net/manual/en/function.header.php
    "header",
    // http://php.net/manual/en/function.header-remove.php
    "header_remove",
    // http://php.net/manual/en/function.ibase-blob-add.php
    "ibase_blob_add",
    // http://php.net/manual/en/function.imagecolorset.php
    "imagecolorset",
    // http://php.net/manual/en/function.imagepalettecopy.php
    "imagepalettecopy",
    // http://php.net/manual/en/imagick.setregistry.php
    "imagick::setregistry",
    // http://php.net/manual/en/function.ini-restore.php
    "ini_restore",
    // http://php.net/manual/en/intlchar.enumcharnames.php
    "intlchar::enumcharnames",
    // http://php.net/manual/en/intlchar.enumchartypes.php
    "intlchar::enumchartypes",
    // http://php.net/manual/en/function.libxml-clear-errors.php
    "libxml_clear_errors",
    // http://php.net/manual/en/function.libxml-set-external-entity-loader.php
    "libxml_set_external_entity_loader",
    // http://php.net/manual/en/function.libxml-set-streams-context.php
    "libxml_set_streams_context",
    // http://php.net/manual/en/function.m-destroyengine.php
    "m_destroyengine",
    // http://php.net/manual/en/function.mailparse-msg-extract-part.php
    "mailparse_msg_extract_part",
    // http://php.net/manual/en/function.maxdb-debug.php
    "maxdb_debug",
    // http://php.net/manual/en/function.maxdb-free-result.php
    "maxdb_free_result",
    // http://php.net/manual/en/function.maxdb-server-end.php
    "maxdb_server_end",
    // http://php.net/manual/en/function.maxdb-stmt-free-result.php
    "maxdb_stmt_free_result",
    // http://php.net/manual/en/function.ming-setcubicthreshold.php
    "ming_setcubicthreshold",
    // http://php.net/manual/en/function.ming-setscale.php
    "ming_setscale",
    // http://php.net/manual/en/function.ming-setswfcompression.php
    "ming_setswfcompression",
    // http://php.net/manual/en/function.ming-useconstants.php
    "ming_useconstants",
    // http://php.net/manual/en/function.ming-useswfversion.php
    "ming_useswfversion",
    // http://php.net/manual/en/function.mongodb.driver.monitoring.addsubscriber.php
    "mongodb\\driver\\monitoring\\addsubscriber",
    // http://php.net/manual/en/function.mongodb.driver.monitoring.removesubscriber.php
    "mongodb\\driver\\monitoring\\addsubscriber",
    // http://php.net/manual/en/mongolog.setlevel.php
    "mongolog::setlevel",
    // http://php.net/manual/en/mongolog.setmodule.php
    "mongolog::setmodule",
    // http://php.net/manual/en/function.mqseries-back.php
    "mqseries_back",
    // http://php.net/manual/en/function.mqseries-begin.php
    "mqseries_begin",
    // http://php.net/manual/en/function.mqseries-close.php
    "mqseries_close",
    // http://php.net/manual/en/function.mqseries-cmit.php
    "mqseries_cmit",
    // http://php.net/manual/en/function.mqseries-conn.php
    "mqseries_conn",
    // http://php.net/manual/en/function.mqseries-connx.php
    "mqseries_connx",
    // http://php.net/manual/en/function.mqseries-disc.php
    "mqseries_disc",
    // http://php.net/manual/en/function.mqseries-get.php
    "mqseries_get",
    // http://php.net/manual/en/function.mqseries-inq.php
    "mqseries_inq",
    // http://php.net/manual/en/function.mqseries-open.php
    "mqseries_open",
    // http://php.net/manual/en/function.mqseries-put.php
    "mqseries_put",
    // http://php.net/manual/en/function.mqseries-put1.php
    "mqseries_put1",
    // http://php.net/manual/en/function.mqseries-set.php
    "mqseries_set",
    // http://php.net/manual/en/function.msession-disconnect.php
    "msession_disconnect",
    // http://php.net/manual/en/function.msession-set-array.php
    "msession_set_array",
    // http://php.net/manual/en/function.mssql-min-error-severity.php
    "mssql_min_error_severity",
    // http://php.net/manual/en/function.mssql-min-message-severity.php
    "mssql_min_message_severity",
    // http://php.net/manual/en/function.mt-srand.php
    "mt_srand",
    // http://php.net/manual/en/mysqli-driver.embedded-server-end.php
    "mysqli_embedded_server_end",
    // http://php.net/manual/en/mysqli-result.free.php
    "mysqli_free_result",
    // http://php.net/manual/en/mysqli.set-local-infile-default.php
    "mysqli_set_local_infile_default",
    // http://php.net/manual/en/mysqli-stmt.data-seek.php
    "mysqli_stmt_data_seek",
    // http://php.net/manual/en/mysqli-stmt.free-result.php
    "mysqli_stmt_free_result",
    // http://php.net/manual/en/function.newt-bell.php
    "newt_bell",
    // http://php.net/manual/en/function.ob-clean.php
    "ob_clean",
    // http://php.net/manual/en/function.ob-flush.php
    "ob_flush",
    // http://php.net/manual/en/function.ob-implicit-flush.php
    "ob_implicit_flush",
    // http://php.net/manual/en/function.oci-internal-debug.php
    "oci_internal_debug",
    // http://php.net/manual/en/function.odbc-close.php
    "odbc_close",
    // http://php.net/manual/en/function.odbc-close-all.php
    "odbc_close_all",
    // http://php.net/manual/en/function.openssl-free-key.php
    "openssl_free_key",
    // http://php.net/manual/en/function.openssl-pkey-free.php
    "openssl_pkey_free",
    // http://php.net/manual/en/function.openssl-x509-free.php
    "openssl_x509_free",
    // http://php.net/manual/en/function.parse-str.php
    "parse_str",
    // http://php.net/manual/en/function.passthru.php
    "passthru",
    // http://php.net/manual/en/function.readline-callback-read-char.php
    "readline_callback_read_char",
    // http://php.net/manual/en/function.readline-on-new-line.php
    "readline_on_new_line",
    // http://php.net/manual/en/function.readline-redisplay.php
    "readline_redisplay",
    // http://php.net/manual/en/function.register-shutdown-function.php
    "register_shutdown_function",
    // http://php.net/manual/en/function.restore-include-path.php
    "restore_include_path",
    // http://php.net/manual/en/function.rewinddir.php
    "rewinddir",
    // http://php.net/manual/en/function.rrdc-disconnect.php
    "rrdc_disconnect",
    // http://php.net/manual/en/function.session-abort.php
    "session_abort",
    // http://php.net/manual/en/function.session-register-shutdown.php
    "session_register_shutdown",
    // http://php.net/manual/en/function.session-reset.php
    "session_reset",
    // http://php.net/manual/en/function.session-set-cookie-params.php
    "session_set_cookie_params",
    // http://php.net/manual/en/function.session-unset.php
    "session_unset",
    // http://php.net/manual/en/function.session-write-close.php
    "session_write_close",
    // http://php.net/manual/en/function.setproctitle.php
    "setproctitle",
    // http://php.net/manual/en/function.shmop-close.php
    "shmop_close",
    // http://php.net/manual/en/function.snmp-set-oid-numeric-print.php
    "snmp_set_oid_numeric_print",
    // http://php.net/manual/en/function.socket-clear-error.php
    "socket_clear_error",
    // http://php.net/manual/en/function.socket-close.php
    "socket_close",
    // http://php.net/manual/en/function.spl-autoload.php
    "spl_autoload",
    // http://php.net/manual/en/function.spl-autoload-call.php
    "spl_autoload_call",
    // http://php.net/manual/en/function.sqlite-busy-timeout.php
    "sqlite_busy_timeout",
    // http://php.net/manual/en/function.sqlite-close.php
    "sqlite_close",
    // http://php.net/manual/en/function.sqlite-create-aggregate.php
    "sqlite_create_aggregate",
    // http://php.net/manual/en/function.sqlite-create-function.php
    "sqlite_create_function",
    // http://php.net/manual/en/function.srand.php
    "srand",
    // http://php.net/manual/en/function.stats-rand-setall.php
    "stats_rand_setall",
    // http://php.net/manual/en/stomp.setreadtimeout.php
    "stomp_set_read_timeout",
    // http://php.net/manual/en/function.stream-bucket-append.php
    "stream_bucket_append",
    // http://php.net/manual/en/function.stream-bucket-prepend.php
    "stream_bucket_prepend",
    // http://php.net/manual/en/function.svn-auth-set-parameter.php
    "svn_auth_set_parameter",
    // http://php.net/manual/en/function.sybase-deadlock-retry-count.php
    "sybase_deadlock_retry_count",
    // http://php.net/manual/en/function.sybase-min-client-severity.php
    "sybase_min_client_severity",
    // http://php.net/manual/en/function.sybase-min-error-severity.php
    "sybase_min_error_severity",
    // http://php.net/manual/en/function.sybase-min-message-severity.php
    "sybase_min_message_severity",
    // http://php.net/manual/en/function.sybase-min-server-severity.php
    "sybase_min_server_severity",
    // http://php.net/manual/en/function.tidy-load-config.php
    "tidy_load_config",
    // http://php.net/manual/en/function.trader-set-compat.php
    "trader_set_compat",
    // http://php.net/manual/en/function.trader-set-unstable-period.php
    "trader_set_unstable_period",
    // http://php.net/manual/en/function.ui-quit.php
    "ui\\quit",
    // http://php.net/manual/en/function.ui-run.php
    "ui\\run",
    // http://php.net/manual/en/function.unregister-tick-function.php
    "unregister_tick_function",
    // http://php.net/manual/en/function.unset.php
    "unset",
    // http://php.net/manual/en/function.usleep.php
    "usleep",
    // http://php.net/manual/en/function.var-dump.php
    "var_dump",
    // http://php.net/manual/en/function.variant-set.php
    "variant_set",
    // http://php.net/manual/en/function.variant-set-type.php
    "variant_set_type",
    // http://php.net/manual/en/function.xhprof-enable.php
    "xhprof_enable",
    // http://php.net/manual/en/function.xhprof-sample-enable.php
    "xhprof_sample_enable",
    // http://php.net/manual/en/function.zip-close.php
    "zip_close"));

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String name = CheckUtils.getFunctionName(tree);
    if (name != null && VOID_FUNCTIONS.contains(name.toLowerCase(Locale.ROOT)) && parentUseValue(tree)) {
      context().newIssue(this, tree.callee(), "Remove this use of the output from " + name + "; " + name + " doesn't return anything.");
    }
    super.visitFunctionCall(tree);
  }

  private static boolean parentUseValue(Tree child) {
    Tree parent = child.getParent();
    Preconditions.checkNotNull(parent);

    if (parent.is(Kind.NEW_EXPRESSION)) {
      return false;

    } else if (parent.is(Kind.PARENTHESISED_EXPRESSION, Kind.ERROR_CONTROL)) {
      return parentUseValue(parent);

    } else if (parent.is(Kind.CONDITIONAL_AND, Kind.ALTERNATIVE_CONDITIONAL_AND, Kind.CONDITIONAL_OR, Kind.ALTERNATIVE_CONDITIONAL_OR)) {
      return child == ((BinaryExpressionTree) parent).leftOperand() || parentUseValue(parent);

    } else if (parent.is(Kind.CONDITIONAL_EXPRESSION)) {
      return child == ((ConditionalExpressionTree) parent).condition() || parentUseValue(parent);

    } else if (parent.is(Kind.EXPRESSION_STATEMENT)) {
      return false;

    } else if (parent.is(Kind.FOR_STATEMENT)) {
      SeparatedList<ExpressionTree> conditions = ((ForStatementTree) parent).condition();
      ExpressionTree lastCondition = conditions.isEmpty() ? null : conditions.get(conditions.size() - 1);
      return child.equals(lastCondition);
    }
    return true;
  }

}
