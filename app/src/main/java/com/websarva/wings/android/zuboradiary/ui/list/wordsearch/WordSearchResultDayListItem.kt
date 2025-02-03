package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.data.database.WordSearchResultListItem;
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WordSearchResultDayListItem extends DiaryDayListBaseItem {

    private final SpannableString title;
    private final ItemNumber itemNumber;
    private final SpannableString itemTitle;
    private final SpannableString itemComment;

    private final String KEY_ITEM_NUMBER = "ItemNumber";
    private final String KEY_ITEM_TITLE = "ItemTitle";
    private final String KEY_ITEM_COMMENT = "ItemComment";

    WordSearchResultDayListItem(
            WordSearchResultListItem listItem, String searchWord, int textColor, int backgroundColor) {
        super(listItem);
        Objects.requireNonNull(listItem);
        Objects.requireNonNull(searchWord);

        String title = listItem.getTitle();
        this.title = toSpannableString(title, searchWord, textColor, backgroundColor);

        Map<String, Object> diaryItem = extractTargetItem(listItem, searchWord);
        Integer itemNumber = (Integer) diaryItem.get(KEY_ITEM_NUMBER);
        Objects.requireNonNull(itemNumber);
        this.itemNumber = new ItemNumber(itemNumber);

        String itemTitle = (String) diaryItem.get(KEY_ITEM_TITLE);
        Objects.requireNonNull(itemTitle);
        this.itemTitle = toSpannableString(itemTitle, searchWord, textColor, backgroundColor);

        String itemComment = (String) diaryItem.get(KEY_ITEM_COMMENT);
        Objects.requireNonNull(itemComment);
        this.itemComment = toSpannableString(itemComment, searchWord, textColor, backgroundColor);
    }

    // 対象ワードをマーキング
    @NonNull
    private SpannableString toSpannableString(String string, String targetWord, int textColor, int backgroundColor) {
        Objects.requireNonNull(string);
        Objects.requireNonNull(targetWord);

        SpannableString spannableString = new SpannableString(string);
        int fromIndex = 0;
        while (string.indexOf(targetWord, fromIndex) != -1) {
            BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(backgroundColor);
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(textColor);
            int start = string.indexOf(targetWord, fromIndex);
            int end = start + targetWord.length();
            spannableString.setSpan(
                    backgroundColorSpan,
                    start,
                    end,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
            );
            spannableString.setSpan(
                    foregroundColorSpan,
                    start,
                    end,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
            );
            fromIndex = end;
        }
        return spannableString;
    }

    @NonNull
    private Map<String, Object> extractTargetItem(WordSearchResultListItem item, String searchWord) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(searchWord);

        String regex = ".*" + searchWord + ".*";
        String[] itemTitles = {
                item.getItem1Title(),
                item.getItem2Title(),
                item.getItem3Title(),
                item.getItem4Title(),
                item.getItem5Title(),
        };
        String[] itemComments = {
                item.getItem1Comment(),
                item.getItem2Comment(),
                item.getItem3Comment(),
                item.getItem4Comment(),
                item.getItem5Comment(),
        };
        int itemNumber = 0;
        String itemTitle = "";
        String itemComment = "";
        for (int i = 0; i < itemTitles.length; i++) {
            // HACK:タイトル、コメントは未入力の場合空文字("")が代入されるはずだが、
            //      nullの項目が存在する為、下記対策をとる。
            //      (例外：項目1のみ入力の場合は、2以降はnullとなる)
            if (itemTitles[i] == null) itemTitles[i] = "";
            if (itemComments[i] == null) itemComments[i] = "";

            if (itemTitles[i].matches(regex)
                    || itemComments[i].matches(regex)) {
                itemNumber = i + 1;
                itemTitle = itemTitles[i];
                itemComment = itemComments[i];
                break;
            }

            // 対象アイテムが無かった場合、アイテムNo.1を抽出
            if (i == (itemTitles.length - 1)) {
                itemNumber = 1;
                itemTitle = itemTitles[0];
                itemComment = itemComments[0];
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put(KEY_ITEM_NUMBER, itemNumber);
        result.put(KEY_ITEM_TITLE, itemTitle);
        result.put(KEY_ITEM_COMMENT, itemComment);
        return result;
    }

    @NonNull
    public SpannableString getTitle() {
        return this.title;
    }

    @NonNull
    public ItemNumber getItemNumber() {
        return this.itemNumber;
    }

    @NonNull
    public SpannableString getItemTitle() {
        return itemTitle;
    }

    @NonNull
    public SpannableString getItemComment() {
        return itemComment;
    }
}
