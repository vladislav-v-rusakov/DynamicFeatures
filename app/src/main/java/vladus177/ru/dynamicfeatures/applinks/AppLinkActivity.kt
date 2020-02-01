package vladus177.ru.dynamicfeatures.applinks

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import vladus177.ru.dynamicfeatures.installdialog.createInstallDialogFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import vladus177.ru.common_feature.Feature
import vladus177.ru.common_presentation.extensions.observe
import javax.inject.Inject

class AppLinkActivity : AppCompatActivity(), HasAndroidInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    @Inject
    lateinit var appLinkViewModel: AppLinkViewModel
    @Inject
    lateinit var handler: Handler

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    private fun launchFeature(feature: String) {
        val launchIntent = appLinkViewModel.getFeature(feature).getLaunchIntent(this)
        startActivity(launchIntent)
        finish()
    }

    private fun onFeatureInstalled(featureInfo: Feature.Info) {
        handler.postDelayed({
            launchFeature(featureInfo.id)
        }, 500)
    }

    private fun handleAppLink(uri: Uri) {
        AndroidInjection.inject(this)
        val feature = uri.pathSegments.firstOrNull()
        if (feature != null) {
            appLinkViewModel.featureInstalled.observe(this, ::onFeatureInstalled)
            if (appLinkViewModel.isFeatureInstalled(feature)) {
                launchFeature(feature)
            } else {
                createInstallDialogFragment(feature).show(supportFragmentManager, "install")
            }
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.data?.let(::handleAppLink) ?: finish()
    }
}