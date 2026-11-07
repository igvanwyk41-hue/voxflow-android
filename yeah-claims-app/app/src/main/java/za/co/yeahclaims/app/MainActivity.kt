package za.co.yeahclaims.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF15243A)
private val Navy = Color(0xFF0D1B2E)
private val Blue = Color(0xFF315779)
private val Gold = Color(0xFFE4B85F)
private val Paper = Color(0xFFF5F7FA)
private val Muted = Color(0xFF667386)
private val Line = Color(0xFFE2E7EE)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { YeahClaimsApp() }
    }
}

@Composable
fun YeahClaimsApp() {
    var page by remember { mutableStateOf("home") }
    var loggedIn by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf("General enquiry") }
    var message by remember { mutableStateOf("") }
    var contactConsent by remember { mutableStateOf(false) }
    var partnerConsent by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf("") }

    MaterialTheme(colorScheme = lightColorScheme(primary = Blue, secondary = Gold, background = Paper)) {
        Surface(Modifier.fillMaxSize(), color = Paper) {
            when (page) {
                "register" -> AuthPage(
                    title = "Create your account",
                    subtitle = "Register to manage your enquiries in one place.",
                    button = "Create account",
                    name = fullName, onName = { fullName = it },
                    email = email, onEmail = { email = it },
                    phone = phone, onPhone = { phone = it },
                    password = password, onPassword = { password = it },
                    confirmPassword = confirmPassword, onConfirmPassword = { confirmPassword = it },
                    showName = true, showPhone = true, showConfirm = true,
                    onSubmit = {
                        feedback = when {
                            fullName.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() -> "Please complete all fields."
                            password.length < 8 -> "Use a password with at least 8 characters."
                            password != confirmPassword -> "Your passwords do not match."
                            else -> "Account registration screen completed. Secure account creation must be connected to the Yeah Claims server before this can be used."
                        }
                    },
                    feedback = feedback,
                    onBack = { page = "home" },
                    onSwitch = { feedback = ""; page = "login" }
                )
                "login" -> AuthPage(
                    title = "Welcome back",
                    subtitle = "Sign in to view your Yeah Claims account.",
                    button = "Sign in",
                    name = fullName, onName = { fullName = it },
                    email = email, onEmail = { email = it },
                    phone = phone, onPhone = { phone = it },
                    password = password, onPassword = { password = it },
                    confirmPassword = confirmPassword, onConfirmPassword = { confirmPassword = it },
                    showName = false, showPhone = false, showConfirm = false,
                    onSubmit = { feedback = "Sign-in will work once secure account authentication is connected." },
                    feedback = feedback,
                    onBack = { page = "home" },
                    onSwitch = { feedback = ""; page = "register" }
                )
                "enquiry" -> EnquiryPage(
                    category = category, onCategory = { category = it },
                    message = message, onMessage = { message = it },
                    contactConsent = contactConsent, onContactConsent = { contactConsent = it },
                    partnerConsent = partnerConsent, onPartnerConsent = { partnerConsent = it },
                    onBack = { page = "home" },
                    onSubmit = {
                        feedback = if (!contactConsent) "Please agree that Yeah Claims may contact you before continuing."
                        else "Your enquiry is ready for review. It has not been sent or stored because the secure submission service is not connected yet."
                        page = "review"
                    }
                )
                "review" -> ReviewPage(feedback = feedback, onBack = { page = "enquiry" }, onHome = { page = "home" })
                "account" -> AccountPage(
                    loggedIn = loggedIn, fullName = fullName, email = email, phone = phone,
                    onRegister = { feedback = ""; page = "register" },
                    onLogin = { feedback = ""; page = "login" },
                    onLogout = { loggedIn = false; page = "home" }
                )
                "help" -> HelpPage(onBack = { page = "home" })
                else -> HomePage(
                    loggedIn = loggedIn, fullName = fullName,
                    onEnquiry = { feedback = ""; page = "enquiry" },
                    onRegister = { feedback = ""; page = "register" },
                    onLogin = { feedback = ""; page = "login" },
                    onAccount = { page = "account" },
                    onHelp = { page = "help" }
                )
            }
        }
    }
}

@Composable
private fun HomePage(
    loggedIn: Boolean, fullName: String,
    onEnquiry: () -> Unit, onRegister: () -> Unit, onLogin: () -> Unit,
    onAccount: () -> Unit, onHelp: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Navy).verticalScroll(rememberScrollState()).padding(22.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("YEAH", color = Gold, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 5.sp)
                Text("CLAIMS", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            }
            TextButton(onClick = onAccount) { Text(if (loggedIn) "MY ACCOUNT" else "ACCOUNT", color = Color.White, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(38.dp))
        Text("A clearer way forward.", color = Color.White, fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("Tell us what you need help with. Submit an enquiry and let the Yeah Claims team guide you through the next step.", color = Color(0xFFD5DFEA), fontSize = 16.sp, lineHeight = 25.sp)
        Spacer(Modifier.height(26.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1B304B)), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("HOW IT WORKS", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(14.dp))
                StepLine("01", "Create your profile", "Register your contact details.")
                StepLine("02", "Tell us what you need", "Choose an enquiry type and provide a short description.")
                StepLine("03", "We follow up", "The team can contact you to discuss the next step.")
            }
        }
        Spacer(Modifier.height(25.dp))
        Button(onClick = onEnquiry, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Gold), shape = RoundedCornerShape(14.dp)) {
            Text("START AN ENQUIRY", color = Navy, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = if (loggedIn) onAccount else onRegister, modifier = Modifier.fillMaxWidth().height(52.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF71839A)), shape = RoundedCornerShape(14.dp)) {
            Text(if (loggedIn) "VIEW MY ACCOUNT" else "CREATE AN ACCOUNT", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Help & information", color = Color(0xFFD5DFEA), modifier = Modifier.clickable { onHelp() })
            Text("yeahclaims.co.za", color = Gold)
        }
        Spacer(Modifier.height(20.dp))
        Text("This app preview is not financial or legal advice. Account services and enquiry submission will be activated when the secure Yeah Claims systems are connected.", color = Color(0xFF9EADBF), fontSize = 11.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun StepLine(number: String, title: String, detail: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
        Text(number, color = Gold, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, modifier = Modifier.width(34.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(detail, color = Color(0xFFBDCAD9), fontSize = 12.sp)
        }
    }
}

@Composable
private fun AuthPage(
    title: String, subtitle: String, button: String,
    name: String, onName: (String) -> Unit,
    email: String, onEmail: (String) -> Unit,
    phone: String, onPhone: (String) -> Unit,
    password: String, onPassword: (String) -> Unit,
    confirmPassword: String, onConfirmPassword: (String) -> Unit,
    showName: Boolean, showPhone: Boolean, showConfirm: Boolean,
    onSubmit: () -> Unit, feedback: String, onBack: () -> Unit, onSwitch: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)) {
        TextButton(onClick = onBack) { Text("←  Back to home") }
        Spacer(Modifier.height(18.dp))
        BrandMark()
        Spacer(Modifier.height(25.dp))
        Text(title, color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Muted, fontSize = 14.sp)
        Spacer(Modifier.height(22.dp))
        if (showName) InputField("Full name", name, onName)
        InputField("Email address", email, onEmail, KeyboardType.Email)
        if (showPhone) InputField("Mobile number", phone, onPhone, KeyboardType.Phone)
        InputField("Password", password, onPassword, KeyboardType.Password)
        if (showConfirm) InputField("Confirm password", confirmPassword, onConfirmPassword, KeyboardType.Password)
        Spacer(Modifier.height(10.dp))
        Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(13.dp)) { Text(button, fontWeight = FontWeight.Bold) }
        if (feedback.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            Text(feedback, color = Blue, fontSize = 13.sp)
        }
        Spacer(Modifier.height(20.dp))
        Text(if (showName) "Already registered? Sign in" else "New to Yeah Claims? Create an account", color = Blue, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onSwitch() })
        Spacer(Modifier.height(20.dp))
        Text("Account registration and sign-in require a secure server connection. This preview does not create or store passwords or accounts.", color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun EnquiryPage(
    category: String, onCategory: (String) -> Unit,
    message: String, onMessage: (String) -> Unit,
    contactConsent: Boolean, onContactConsent: (Boolean) -> Unit,
    partnerConsent: Boolean, onPartnerConsent: (Boolean) -> Unit,
    onBack: () -> Unit, onSubmit: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)) {
        TextButton(onClick = onBack) { Text("←  Back to home") }
        Spacer(Modifier.height(15.dp))
        BrandMark()
        Spacer(Modifier.height(22.dp))
        Text("Start an enquiry", color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Bold)
        Text("Choose the area you need help with and tell us a little more.", color = Muted)
        Spacer(Modifier.height(22.dp))
        Text("Enquiry type", color = Ink, fontWeight = FontWeight.Bold)
        listOf("General enquiry", "Financial services", "Insurance enquiry", "Claims assistance", "Other").forEach { option ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onCategory(option) }) {
                RadioButton(selected = category == option, onClick = { onCategory(option) })
                Text(option, color = Ink)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("How can we help?", color = Ink, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = message, onValueChange = onMessage, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp), placeholder = { Text("Briefly describe your enquiry") }, shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.Top) {
            Checkbox(checked = contactConsent, onCheckedChange = onContactConsent)
            Text("I agree that Yeah Claims may contact me about this enquiry.", color = Ink, fontSize = 13.sp, modifier = Modifier.padding(top = 11.dp))
        }
        Row(verticalAlignment = Alignment.Top) {
            Checkbox(checked = partnerConsent, onCheckedChange = onPartnerConsent)
            Text("I separately agree that my details may be shared with relevant partner organisations for this enquiry. This is optional.", color = Ink, fontSize = 13.sp, modifier = Modifier.padding(top = 11.dp))
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onSubmit, enabled = contactConsent, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(13.dp)) { Text("CONTINUE", fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(14.dp))
        Text("Partner-sharing consent is optional and is separate from permission to contact you. This preview does not send or store your information.", color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun ReviewPage(feedback: String, onBack: () -> Unit, onHome: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(25.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        BrandMark()
        Spacer(Modifier.height(26.dp))
        Text("Enquiry preview", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(feedback, color = Muted, fontSize = 15.sp, lineHeight = 22.sp)
        Spacer(Modifier.height(26.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("EDIT ENQUIRY") }
        TextButton(onClick = onHome) { Text("Back to home") }
    }
}

@Composable
private fun AccountPage(
    loggedIn: Boolean, fullName: String, email: String, phone: String,
    onRegister: () -> Unit, onLogin: () -> Unit, onLogout: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(22.dp)) {
        TextButton(onClick = onLogout) { Text("←  Home") }
        BrandMark()
        Spacer(Modifier.height(25.dp))
        Text("My account", color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Bold)
        if (loggedIn) {
            Text(fullName, color = Ink, fontWeight = FontWeight.Bold)
            Text(email, color = Muted)
            Text(phone, color = Muted)
            Text("Your account profile is currently a local preview.", color = Muted, fontSize = 12.sp)
        } else {
            Text("Sign in or create an account to manage your profile and enquiries.", color = Muted)
            Spacer(Modifier.height(20.dp))
            Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("SIGN IN") }
            OutlinedButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) { Text("CREATE ACCOUNT") }
        }
    }
}

@Composable
private fun HelpPage(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp)) {
        TextButton(onClick = onBack) { Text("←  Back to home") }
        BrandMark()
        Spacer(Modifier.height(25.dp))
        Text("Help & information", color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Bold)
        Text("Yeah Claims helps you make an enquiry and connect with the team about possible next steps.", color = Muted, lineHeight = 22.sp)
        Spacer(Modifier.height(16.dp))
        Text("Website", color = Ink, fontWeight = FontWeight.Bold)
        Text("www.yeahclaims.co.za", color = Blue)
        Spacer(Modifier.height(16.dp))
        Text("Important", color = Ink, fontWeight = FontWeight.Bold)
        Text("This app preview does not assess claims, provide financial or legal advice, submit information to third parties, or guarantee any outcome.", color = Muted, lineHeight = 20.sp)
    }
}

@Composable
private fun BrandMark() {
    Column {
        Text("YEAH", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
        Text("CLAIMS", color = Ink, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
    }
}

@Composable
private fun InputField(label: String, value: String, onValue: (String) -> Unit, keyboard: KeyboardType = KeyboardType.Text) {
    Column(Modifier.padding(bottom = 13.dp)) {
        Text(label, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.height(5.dp))
        OutlinedTextField(
            value = value, onValueChange = onValue, modifier = Modifier.fillMaxWidth(),
            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            shape = RoundedCornerShape(12.dp)
        )
    }
}
