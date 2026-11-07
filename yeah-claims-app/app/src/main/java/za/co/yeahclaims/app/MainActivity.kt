package za.co.yeahclaims.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF101D32)
private val Blue = Color(0xFF24476B)
private val Gold = Color(0xFFE2B45B)
private val Mist = Color(0xFFF3F6FA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { YeahClaimsApp() }
    }
}

@Composable
fun YeahClaimsApp() {
    var screen by remember { mutableStateOf("home") }
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var enquiry by remember { mutableStateOf("General enquiry") }
    var consent by remember { mutableStateOf(false) }
    var contactConsent by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = lightColorScheme(primary = Blue, secondary = Gold, background = Mist)) {
        Surface(Modifier.fillMaxSize(), color = Mist) {
            when (screen) {
                "form" -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)) {
                    TextButton(onClick = { screen = "home" }) { Icon(Icons.Default.ArrowBack, null); Spacer(Modifier.width(6.dp)); Text("Back") }
                    Spacer(Modifier.height(18.dp))
                    Text("LET'S GET STARTED", color = Blue, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Request a callback", color = Navy, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text("Share a few details and our team can get in touch.", color = Color.DarkGray)
                    Spacer(Modifier.height(22.dp))
                    Field("First name", first, { first = it })
                    Field("Surname", last, { last = it })
                    Field("Mobile number", phone, { phone = it }, KeyboardType.Phone)
                    Field("Email address", email, { email = it }, KeyboardType.Email)
                    Text("What is your enquiry about?", fontWeight = FontWeight.SemiBold, color = Navy)
                    Spacer(Modifier.height(8.dp))
                    listOf("General enquiry", "Financial services", "Insurance enquiry", "Other").forEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = enquiry == it, onClick = { enquiry = it })
                            Text(it)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Checkbox(consent, { consent = it })
                        Text("I agree that Yeah Claims may contact me about this enquiry.", fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.Top) {
                        Checkbox(contactConsent, { contactConsent = it })
                        Text("I separately agree to the sharing of my details with relevant partner organisations for this enquiry. (Optional)", fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { screen = "sent" },
                        enabled = first.isNotBlank() && last.isNotBlank() && phone.isNotBlank() && consent,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Submit enquiry", fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.height(12.dp))
                    Text("Demo build: this form does not transmit or save your information yet. A secure, approved backend and final privacy wording must be connected before public use.", color = Color(0xFF6A7482), fontSize = 12.sp)
                }
                "sent" -> Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF27835D), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(18.dp))
                    Text("Thank you, $first", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Navy)
                    Text("Your demo enquiry has been completed.", color = Color.DarkGray)
                    Text("No information has been sent to Yeah Claims. Online submission will be enabled after the secure backend is configured.", color = Color.Gray, fontSize = 13.sp)
                    Spacer(Modifier.height(22.dp))
                    Button(onClick = { screen = "home"; first=""; last=""; phone=""; email=""; consent=false; contactConsent=false }, modifier = Modifier.fillMaxWidth()) { Text("Return home") }
                }
                else -> Column(Modifier.fillMaxSize().background(Navy).verticalScroll(rememberScrollState()).padding(26.dp)) {
                    Spacer(Modifier.height(24.dp))
                    Text("YEAH", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 5.sp)
                    Text("CLAIMS", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(42.dp))
                    Text("A clearer way\nforward.", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Tell us what you need help with. Our team can contact you to discuss the next step.", color = Color(0xFFD4DDE8), fontSize = 16.sp, lineHeight = 24.sp)
                    Spacer(Modifier.height(30.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2D47)), shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.padding(20.dp)) {
                            Text("HOW IT WORKS", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("1. Send an enquiry", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("2. We review your details", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("3. We contact you about possible next steps", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(28.dp))
                    Button(onClick = { screen = "form" }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Gold), shape = RoundedCornerShape(14.dp)) {
                        Text("Start an enquiry", color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("yeahclaims.co.za", color = Color(0xFFB9C7D8), fontSize = 13.sp)
                    Spacer(Modifier.height(24.dp))
                    Text("This initial preview is not a financial or legal advice service. No claim assessment or partner referral is performed in this build.", color = Color(0xFF9EADBF), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onValue: (String) -> Unit, keyboard: KeyboardType = KeyboardType.Text) {
    Column {
        Text(label, color = Navy, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(value, onValue, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboard), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(14.dp))
    }
}
