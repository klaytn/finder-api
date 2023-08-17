package io.klaytn.finder.interfaces.rest.api.view.mapper.appuser

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set4.AppUserKey
import io.klaytn.finder.interfaces.rest.api.view.model.appuser.AppUserKeyView
import org.springframework.stereotype.Component

@Component
class AppUserKeyToViewMapper : Mapper<AppUserKey, AppUserKeyView> {
    override fun transform(source: AppUserKey) =
        AppUserKeyView(
            appUserId = source.appUserId,
            accessKey = source.accessKey,
            name = source.name,
            description = source.description,
            activatedAt = source.activatedAt,
            deactivatedAt = source.deactivatedAt
        )
}