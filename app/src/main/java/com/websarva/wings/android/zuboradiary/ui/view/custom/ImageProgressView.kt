package com.websarva.wings.android.zuboradiary.ui.view.custom

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

    private val defaultIconDrawable: Drawable?
    private val errorIconDrawable: Drawable?
    private val iconColorInt: Int

    init {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ImageProgressView,
            defStyleAttr,
            defStyleRes
        )

        try {
            val defaultIconRes = typedArray.getResourceId(
                R.styleable.ImageProgressView_defaultIcon,
                R.drawable.ic_image_24px
            )
            defaultIconDrawable = ContextCompat.getDrawable(context, defaultIconRes)

            val errorIconRes = typedArray.getResourceId(
                R.styleable.ImageProgressView_errorIcon,
                R.drawable.ic_hide_image_24px
            )
            errorIconDrawable = ContextCompat.getDrawable(context, errorIconRes)

            iconColorInt = typedArray.getColor(
                R.styleable.ImageProgressView_iconTint,
                run {
                    val typedValue = TypedValue()
                    context.theme.resolveAttribute(
                        com.google.android.material.R.attr.colorOnSurfaceVariant,
                        typedValue,
                        true
                    )
                    typedValue.data
                }
            )

            val scaleTypeIndex = typedArray.getInt(
                R.styleable.ImageProgressView_imageScaleType,
                -1
            )
            if (scaleTypeIndex >= 0) {
                binding.image.scaleType =
                    ImageView.ScaleType.entries.toTypedArray()[scaleTypeIndex]
            }

            val contentDescFromAttr = typedArray.getString(R.styleable.ImageProgressView_imageContentDescription)
            binding.image.contentDescription = contentDescFromAttr ?: ""

            val adjustViewBounds = typedArray.getBoolean(
                R.styleable.ImageProgressView_imageAdjustViewBounds,
                false
            )
            binding.image.adjustViewBounds = adjustViewBounds

        } finally {
            typedArray.recycle()
        }

        binding.image.apply {
            setImageDrawable(defaultIconDrawable)
            setColorFilter(iconColorInt)
        }
    }

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

    /**
     * 内部のImageViewにクリックリスナーを設定する。
     *
     * @param listener 設定するクリックリスナー、またはnull。
     */
    fun setImageOnClickListener(listener: OnClickListener?) {
        binding.image.setOnClickListener(listener)
    }
}
