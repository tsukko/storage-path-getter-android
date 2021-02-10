package jp.co.integrityworks.storagepathgetter

import android.util.Log

import java.util.Locale

/**
 * ログのユーティリティクラス
 */
object Logger {
    /**
     * Debugログを出力する
     *
     * @param tag     タグ
     * @param message メッセージ
     * @since 001
     */
    fun debug(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, generateMessage(message, 2))
        }
    }

    /**
     * Infoログを出力する
     *
     * @param tag     タグ
     * @param message メッセージ
     * @since 001
     */
    fun info(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, generateMessage(message, 2))
        }
    }

    /**
     * Warningログを出力する
     *
     * @param tag     タグ
     * @param message メッセージ
     * @since 001
     */
    fun warn(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, generateMessage(message, 2))
        }
    }

    /**
     * Errorログを出力する
     *
     * @param tag     タグ
     * @param message メッセージ
     * @since 001
     */
    fun error(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, generateMessage(message, 2))
        }
    }

    /**
     * 例外付きのErrorログを出力する
     *
     * @param tag     タグ
     * @param message メッセージ
     * @param error   例外
     * @since 001
     */
    fun error(tag: String, message: String, error: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, generateMessage(message, 2), error)
        }
    }

    /**
     * ファイル名と行数つきのメッセージを生成する
     *
     * @param message メッセージ
     * @return ファイル名と行数つきのメッセージ
     * @since 001
     */
    fun generateMessage(message: String): String {
        return generateMessage(message, 2)
    }

    private fun generateMessage(message: String, nest: Int): String {
        var nest = nest
        if (nest < 1) {
            nest = 1
        }
        val format = "[%s (L:%d)] %s"
        val element = Throwable().stackTrace[nest]
        return String.format(Locale.getDefault(), format, element.fileName, element.lineNumber, message)
    }
}
