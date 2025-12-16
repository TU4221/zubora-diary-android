package com.websarva.wings.android.zuboradiary.ui.common.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import coil3.asImage
import coil3.dispose
import coil3.load
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.databinding.ViewImageProgressBinding
import java.io.File

/**
 * 画像の読み込み中にプログレスインジケーターを表示するカスタムView。
 *
 * このViewは、`ImageView`と`ProgressBar`をカプセル化し、指定されたファイルパスから画像を非同期に読み込む。
 * 読み込み中はプログレスバーを表示し、成功または失敗に応じて画像やアイコンを切り替える責務を持つ。
 * XMLレイアウトから、`defaultIcon`、`errorIcon`、`iconTint`などのカスタム属性を設定できる。
 */
internal class ImageProgressView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: ViewImageProgressBinding =
        ViewImageProgressBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    /** 読み込み前に表示されるデフォルトのアイコン。 */
    private val defaultIconDrawable: Drawable?

    /** 読み込み失敗時に表示されるエラーアイコン。 */
    private val errorIconDrawable: Drawable?

    /** デフォルトアイコンおよびエラーアイコンに適用される色。 */
    private val iconColorInt: Int

    init {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ImageProgressView,
            defStyleAttr,
            defStyleRes
        )

        try {
            // デフォルトアイコンの取得
            val defaultIconRes = typedArray.getResourceId(
                R.styleable.ImageProgressView_defaultIcon,
                R.drawable.ic_image_24px
            )
            defaultIconDrawable = ContextCompat.getDrawable(context, defaultIconRes)

            // エラーアイコンの取得
            val errorIconRes = typedArray.getResourceId(
                R.styleable.ImageProgressView_errorIcon,
                R.drawable.ic_hide_image_24px
            )
            errorIconDrawable = ContextCompat.getDrawable(context, errorIconRes)

            // アイコンの色の取得（未指定の場合はアプリケーションのデフォルトテーマの`colorOnSurfaceVariant`を使用）
            val typedValue = TypedValue()
            context.theme.resolveAttribute(
                com.google.android.material.R.attr.colorOnSurfaceVariant,
                typedValue,
                true
            )
            iconColorInt = typedArray.getColor(
                R.styleable.ImageProgressView_iconTint,
                typedValue.data
            )

            // ImageViewのScaleTypeの取得と設定
            val scaleTypeIndex = typedArray.getInt(
                R.styleable.ImageProgressView_imageScaleType,
                -1
            )
            if (scaleTypeIndex >= 0) {
                binding.image.scaleType =
                    ImageView.ScaleType.entries.toTypedArray()[scaleTypeIndex]
            }

            // ContentDescriptionの取得と設定
            val contentDescFromAttr =
                typedArray.getString(R.styleable.ImageProgressView_imageContentDescription)
            binding.image.contentDescription = contentDescFromAttr ?: ""

            // AdjustViewBounds属性の取得と設定
            val adjustViewBounds = typedArray.getBoolean(
                R.styleable.ImageProgressView_imageAdjustViewBounds,
                false
            )
            binding.image.adjustViewBounds = adjustViewBounds

        } finally {
            // 取得した属性値をシステムに解放する
            typedArray.recycle()
        }

        with(binding.image) {
            setImageDrawable(defaultIconDrawable)
            setColorFilter(iconColorInt)
        }
    }

    /**
     * 指定されたファイルパスから画像を非同期に読み込み、Viewに表示する。
     *
     * - パスがnullの場合、デフォルトアイコンを表示する。
     * - 読み込み中はプログレスバーを表示する。
     * - 読み込みに成功した場合、画像を表示し、アイコン用のカラーフィルタをクリアする。
     * - 読み込みに失敗した場合、エラーアイコンを表示し、カラーフィルタを適用する。
     *
     * @param imagePath 読み込む画像のファイルパス。nullの場合はデフォルトアイコンが表示される。
     */
     fun loadImage(imagePath: String?) {
        val imageView = binding.image
        val progressView = binding.progress

        val data =
            if (imagePath == null) {
                defaultIconDrawable
            } else {
                File(imagePath)
            }
        val currentDrawable = imageView.drawable
        imageView.dispose()
        imageView.doOnPreDraw { 
            imageView.load(data) {
                // プレースホルダ画像設定
                currentDrawable?.let {
                    placeholder(currentDrawable.asImage())
                }
                // エラー画像設定
                errorIconDrawable?.let { error(errorIconDrawable.asImage()) }
                // プログレッス可視、カラー設定
                listener(
                    onStart = {
                        if (data is File) progressView.visibility = VISIBLE
                    },
                    onSuccess = { request, _ ->
                        progressView.visibility = INVISIBLE
                        if (request.data is File) {
                            imageView.clearColorFilter()
                        } else {
                            imageView.setColorFilter(iconColorInt)
                        }
                    },
                    onError = { _, _ ->
                        progressView.visibility = INVISIBLE
                        imageView.setColorFilter(iconColorInt)
                    }
                )
            }
        }
    }
}
