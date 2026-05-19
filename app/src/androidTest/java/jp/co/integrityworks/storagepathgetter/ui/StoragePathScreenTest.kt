package jp.co.integrityworks.storagepathgetter.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.integrityworks.storagepathgetter.ui.screens.StoragePathScreen
import jp.co.integrityworks.storagepathgetter.ui.theme.StoragePathGetterTheme
import jp.co.integrityworks.storagepathgetter.util.Utils
import org.junit.Rule
import org.junit.Test

class StoragePathScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun storagePathScreen_DisplaysTitle() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val utils = Utils(context)

        composeTestRule.setContent {
            StoragePathGetterTheme {
                StoragePathScreen(
                    util = utils,
                    onRequestPermission = {}
                )
            }
        }

        // 画面に「ストレージ情報の取得」に関連するテキストが表示されているか確認
        // (R.string.text_information の内容を確認)
        composeTestRule.onNodeWithText("ストレージのパス情報を取得します。", substring = true)
            .assertExists()
    }

    @Test
    fun storagePathScreen_DisplaysButtons() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val utils = Utils(context)

        composeTestRule.setContent {
            StoragePathGetterTheme {
                StoragePathScreen(
                    util = utils,
                    onRequestPermission = {}
                )
            }
        }

        // 再取得ボタンとクリアボタンが存在することを確認
        composeTestRule.onNodeWithText("再取得").assertExists()
        composeTestRule.onNodeWithText("クリア").assertExists()
    }
}
