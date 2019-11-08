package zielu.gittoolbox.config

import com.intellij.openapi.vcs.actions.ShortNameType
import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.ResBundle

internal enum class AuthorNameType(private val labelSupplier: () -> String) {
  INITIALS(ShortNameType.INITIALS::getDescription),
  LASTNAME(ShortNameType.LASTNAME::getDescription),
  FIRSTNAME(ShortNameType.FIRSTNAME::getDescription),
  FULL(ShortNameType.NONE::getDescription),
  EMAIL({ ResBundle.message("author.name.type.email") }),
  EMAIL_USER({ ResBundle.message("author.name.type.email.user") });

  @Transient
  fun getDisplayLabel() = labelSupplier.invoke()

  companion object {
    @JvmStatic
    val inlineBlame = values().toList()
    @JvmStatic
    val statusBlame = values().toList()
  }
}
