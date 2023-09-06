package xyz.lucasteel.kitme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import xyz.lucasteel.kitme.ui.theme.justFamily

@Composable
fun InfoScreen(navController: NavController) {
    Surface(Modifier.fillMaxSize()) {
        InfoScreenContent(navController = navController)
    }
}

@Composable
fun InfoScreenContent(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        BackButton(navController = navController)
        Text(
            text = "About the KitMe project",
            fontFamily = justFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Hi! I'm Luca, a 16-year-old from Romania with a passion for programming and computers. KitMe started as a simple summer project, but it ended up being one of the most challenging adventures of my life. I learned so much about mobile development along the way and I can't wait to start a new project. If you want to support the KitMe project and my education, you can do so by using the links below. And lastly, thanks a ton for downloading and using KitMe. <3",
            fontFamily = justFamily,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 5.dp, end = 5.dp),
            textAlign = TextAlign.Center
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DonateButton(
                text = "Donate via PayPal",
                url = "https://paypal.me/lucastrambeanu?country.x=RO&locale.x=en_US"
            )
            DonateButton(text = "Buy Me a Coffee", url = "https://www.buymeacoffee.com/lucasteel")
        }
        Image(
            painter = painterResource(R.drawable.login_kitme_cropped),
            contentDescription = "kitten photos",
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun DonateButton(text: String, url: String) {
    val uriHandler = LocalUriHandler.current
    OutlinedButton(onClick = { uriHandler.openUri(url) }) {
        Text(text = text, fontFamily = justFamily)
    }
}