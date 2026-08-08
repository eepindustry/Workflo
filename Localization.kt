package com.example.ui.utils

enum class Language {
    ENGLISH, HINDI
}

fun getText(english: String, language: Language): String {
    if (language == Language.ENGLISH) return english
    return when(english) {
        "Progress" -> "प्रगति"
        "Wallet" -> "वॉलेट"
        "Contest" -> "प्रतियोगिता"
        "Info" -> "जानकारी"
        "Contact" -> "संपर्क"
        "Play Quiz" -> "क्विज़ खेलें"
        "Current Level" -> "वर्तमान स्तर"
        "Level" -> "स्तर"
        "Wallet Balance" -> "वॉलेट बैलेंस"
        "Redeem WCoins" -> "WCoins रिडीम करें"
        "Enter Mobile Number" -> "मोबाइल नंबर दर्ज करें"
        "Submit" -> "जमा करें"
        "Contest is waiting to join" -> "प्रतियोगिता जुड़ने की प्रतीक्षा कर रही है"
        "Rank" -> "रैंक"
        "User" -> "उपयोगकर्ता"
        "Winning Prize" -> "जीतने वाला इनाम"
        "You" -> "आप"
        "Information" -> "जानकारी"
        "Thank you for joining Water family, please support us" -> "वाटर फैमिली में शामिल होने के लिए धन्यवाद, कृपया हमारा समर्थन करें"
        "Message us for feedback:\ngodffm0202@gmail.com" -> "फीडबैक के लिए हमें मैसेज करें:\ngodffm0202@gmail.com"
        "Contact / Support" -> "संपर्क / समर्थन"
        "Need help? Email us at:" -> "मदद चाहिए? हमें ईमेल करें:"
        "Score" -> "स्कोर"
        "Good!" -> "बहुत अच्छा!"
        "Time's Up!" -> "समय समाप्त!"
        "Out of Lives!" -> "लाइफ खत्म!"
        "Restart (Score 0)" -> "रीस्टार्ट (स्कोर 0)"
        "Use Extra Life (Watch Ad)" -> "अतिरिक्त लाइफ (विज्ञापन देखें)"
        "Use Extra Life (Ad-Free)" -> "अतिरिक्त लाइफ (बिना विज्ञापन)"
        "Daily 60 Ad limit reached! Extra Life is now AD-FREE." -> "दैनिक 60 विज्ञापन सीमा पूरी हो गई! अब अतिरिक्त लाइफ बिना विज्ञापन के है।"
        "This limit resets at 12:00 AM (midnight)." -> "यह सीमा रात 12:00 बजे रीसेट होगी।"
        "Quit Game" -> "गेम छोड़ें"
        "Your current level: " -> "आपका वर्तमान स्तर: "
        "Total time worked: " -> "कुल कार्य समय: "
        "Level Passed!" -> "स्तर पार!"
        "Continue" -> "जारी रखें"
        "Out of 1000 players" -> "1000 खिलाड़ियों में से"
        "Contest is waiting for minimum players to join 1000" -> "प्रतियोगिता को 1000 न्यूनतम खिलाड़ियों के जुड़ने का इंतजार है"
        "You need to reach Level 100 to join the Contest.\nYour current level: " -> "प्रतियोगिता में शामिल होने के लिए आपको 100 स्तर तक पहुंचने की आवश्यकता है।\nआपका वर्तमान स्तर: "
        "Goal" -> "लक्ष्य"
        "Solve" -> "हल करें"
        "Option" -> "विकल्प"
        "Current balance: " -> "वर्तमान शेष: "
        "Name" -> "नाम"
        else -> english
    }
}
