package com.wrapper.apibridge.api.dto

import java.io.Serializable

class BookItem(
    val kind: String,
    val id: String,
    val etag: String,
    val selfLink: String,
    val volumeInfo: VolumeInfo,
    val saleInfo: SaleInfo,
    val accessInfo: AccessInfo,
    val searchInfo: SearchInfo,
) : Serializable

class VolumeInfo(
    val title: String,
    val subtitle: String,
    val authors: List<String>,
    val publishedDate: String,
    val description: String,
    val industryIdentifiers: List<IndustryIdentifiers>,
    val readingModes: ReadingModes,
    val pageCount: Long,
    //TODO(filter type and create enum)
    val printType: String,
    val categories: List<String>,
    val maturityRating: String,
    val allowAnonLogging: Boolean,
    val contentVersion: String,
    val panelizationSummary: PanelizationSummary,
    val imageLinks: ImageLinks,
    val language: String,
    val previewLink: String,
    val infoLink: String,
    val canonicalVolumeLink: String,
) : Serializable

class SaleInfo(
    val country: String,
    val saleability: String,
    val isEbook: Boolean,
    val buyLink: String,
) : Serializable

class AccessInfo(
    val country: String,
    val viewability: String,
    val embeddable: Boolean,
    val publicDomain: Boolean,
    val textToSpeechPermission: String,
    val epub: Epub,
    val pdf: Pdf,
    val webReaderLink: String?,
    val accessViewStatus: String?,
    val quoteSharingAllowed: Boolean
) : Serializable

class SearchInfo(
    val textSnippet: String,
) : Serializable

class IndustryIdentifiers(
    val type: String,
    val identifier: String,
) : Serializable

class ReadingModes(
    val text: Boolean,
    val image: Boolean,
) : Serializable

class PanelizationSummary(
    val containsEpubBubbles: Boolean,
    val containsImageBubbles: Boolean,
) : Serializable

class ImageLinks(
    val smallThumbnail: String,
    val thumbnail: String,
) : Serializable

class Epub(
    val isAvailable: Boolean,
    val downloadLink: String?

) : Serializable

class Pdf(
    val isAvailable: Boolean,
    val downloadLink: String?
) : Serializable
