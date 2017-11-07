/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S3699")
public class UseOfEmptyReturnValueCheck extends PHPVisitorCheck {

  private static final Set<String> VOID_FUNCTIONS = new HashSet<>(Arrays.asList(
    "accelerator_set_status",
    "clearstatcache",
    "closedir",
    "curl_close",
    "curl_multi_close",
    "curl_reset",
    "curl_share_close",
    "dba_close",
    "debug_print_backtrace",
    "debug_zval_dump",
    "define_syslog_variables",
    "enchant_dict_add_to_personal",
    "enchant_dict_add_to_session",
    "enchant_dict_store_replacement",
    "event_base_free",
    "event_buffer_fd_set",
    "event_buffer_free",
    "event_buffer_timeout_set",
    "event_buffer_watermark_set",
    "event_free",
    "event_timer_set",
    "fann_print_error",
    "fann_reset_errno",
    "fann_reset_errstr",
    "fann_set_error_log",
    "flush",
    "gc_disable",
    "gc_enable",
    "gmp_clrbit",
    "gmp_setbit",
    "header",
    "header_remove",
    "hex2bin",
    "http_redirect",
    "http_throttle",
    "ibase_blob_add",
    "imagecolorset",
    "imagepalettecopy",
    "ini_restore",
    "java_last_exception_clear",
    "java_set_ignore_case",
    "java_throw_exceptions",
    "libxml_clear_errors",
    "libxml_set_external_entity_loader",
    "libxml_set_streams_context",
    "ming_setcubicthreshold",
    "ming_setscale",
    "ming_setswfcompression",
    "ming_useconstants",
    "ming_useswfversion",
    "mongolog::setlevel",
    "mongolog::setmodule",
    "monitor_custom_event",
    "monitor_httperror_event",
    "monitor_pass_error",
    "monitor_set_aggregation_hint",
    "ms_iogetstdoutbufferstring",
    "ms_ioinstallstdinfrombuffer",
    "ms_ioinstallstdouttobuffer",
    "ms_ioresethandlers",
    "ms_iostripstdoutbuffercontentheaders",
    "ms_reseterrorlist",
    "mssql_min_error_severity",
    "mssql_min_message_severity",
    "mt_srand",
    "mysqli_free_result",
    "mysqli_set_local_infile_default",
    "mysqli_stmt_data_seek",
    "mysqli_stmt_free_result",
    "ncurses_bkgdset",
    "ncurses_filter",
    "ncurses_getmaxyx",
    "ncurses_getyx",
    "ncurses_init",
    "ncurses_noqiflush",
    "ncurses_qiflush",
    "ncurses_timeout",
    "ncurses_update_panels",
    "ncurses_use_env",
    "newrelic_background_job",
    "newrelic_capture_params",
    "newrelic_end_of_transaction",
    "newrelic_ignore_apdex",
    "newrelic_ignore_transaction",
    "newrelic_notice_error",
    "newrelic_record_custom_event",
    "ob_clean",
    "ob_flush",
    "ob_implicit_flush",
    "oci_internal_debug",
    "odbc_close",
    "odbc_close_all",
    "openssl_free_key",
    "openssl_pkey_free",
    "openssl_x509_free",
    "output_cache_disable",
    "output_cache_disable_compression",
    "output_cache_stop",
    "parse_str",
    "passthru",
    "pcntl_exec",
    "ps_unreserve_prefix___halt_compiler",
    "ps_unreserve_prefix_die",
    "ps_unreserve_prefix_exit",
    "ps_unreserve_prefix_unset",
    "readline_callback_read_char",
    "readline_on_new_line",
    "readline_redisplay",
    "register_shutdown_function",
    "restore_include_path",
    "rewind",
    "rewinddir",
    "rrd_disconnect",
    "sem_get",
    "session_set_cookie_params",
    "session_unset",
    "session_write_close",
    "shmop_close",
    "snmp_set_oid_numeric_print",
    "socket_clear_error",
    "socket_close",
    "socket_import_stream",
    "spl_autoload",
    "spl_autoload_call",
    "sqlite_busy_timeout",
    "sqlite_close",
    "sqlite_create_aggregate",
    "sqlite_create_function",
    "srand",
    "stream_bucket_append",
    "stream_bucket_prepend",
    "svn_auth_set_parameter",
    "sybase_deadlock_retry_count",
    "sybase_min_client_severity",
    "sybase_min_server_severity",
    "unregister_tick_function",
    "usleep",
    "var_dump",
    "variant_set",
    "variant_set_type",
    "xcache_clear_cache",
    "xcache_coverager_start",
    "xcache_coverager_stop",
    "xdebug_debug_zval",
    "xdebug_debug_zval_stdout",
    "xdebug_disable",
    "xdebug_dump_superglobals",
    "xdebug_enable",
    "xdebug_start_code_coverage",
    "xdebug_start_error_collection",
    "xdebug_start_function_monitor",
    "xdebug_start_trace",
    "xdebug_stop_code_coverage",
    "xdebug_stop_error_collection",
    "xdebug_stop_function_monitor",
    "xdebug_stop_trace",
    "xdebug_var_dump",
    "zend_runtime_obfuscate",
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
    if (parent.is(Tree.Kind.PARENTHESISED_EXPRESSION, Tree.Kind.ERROR_CONTROL)) {
      return parentUseValue(parent);
    } else if (parent.is(Tree.Kind.EXPRESSION_STATEMENT)) {
      return false;
    } else if (parent.is(Tree.Kind.FOR_STATEMENT)) {
      SeparatedList<ExpressionTree> conditions = ((ForStatementTree) parent).condition();
      ExpressionTree lastCondition = conditions.isEmpty() ? null : conditions.get(conditions.size() - 1);
      return child.equals(lastCondition);
    }
    return true;
  }

}
