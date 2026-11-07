let servicers = [
    "Priya Patel",
    "Aarav Sharma",
    "Ananya Reddy",
    "Rohan Verma",
    "Sneha Iyer",
    "Vikram Malhotra",
    "Kavya Nair",
    "Arjun Rao",
    "Divya Krishnan",
    "Aditya Joshi",
    "Meera Sundaram",
    "Siddharth Nair"
];
let index = Math.round(Math.random() * (servicers.length - 1));
let servicer = servicers[index];

// Welcome greeting
addServerMessage(`Hi, welcome to Fleet Management Support! I'm ${servicer}, your customer support executive. How can I assist you with your booking or delivery today?`);

function addServerMessage(message) {

    let messageRes = `
    <div class="message-container-left">
        <div class="avatar">
            <img src="./logo.png" alt="Logo" class="logoImage">
        </div>
        <div class="message-area">
            <div class="message-topic">
                <div class="message-sender">${servicer}</div>
                <div class="message-time">${getTime()}</div>
            </div>
            <div class="message">${cleanResponse(message)}</div>
        </div>
    </div>
    `
    $("#char-area-bottom").before(messageRes);
    scrollToBottom();
}

function getTime() {
    let newDate = new Date();
    let format = (x) => x.toString().padStart(2, "0");
    let time = format(newDate.getHours()) + ":" + format(newDate.getMinutes()) + ":" + format(newDate.getSeconds());
    return time;
}

function addCustomerMessage(message) {

    let messageRes = `
    <div class="message-container-right">
        <div class="message-area">
            <div class="message-topic">
                <div class="message-time">${getTime()}</div>
            </div>
            <div class="message">${message}</div>
        </div>
    </div>
    `
    $("#char-area-bottom").before(messageRes);
}


$("#sendBtn").click(function () {
    sendMessage();
});

let $inputBox = $("#inputBox");

$inputBox.keypress(function (e) {
    if (e.which === 13) {
        sendMessage();
    }
});

async function sendMessage() {
    let message = $inputBox.val();
    if (message.length === 0) {
        return;
    }
    addCustomerMessage(message);
    $inputBox.val("");
    scrollToBottom();

    await getResponse().then(function (data) {
        addServerMessage(data["response"]);
    }).catch(function (error) {
        netWorkError();
    });

}

function generateFleetSupportResponse(userText) {
    let lower = (userText || "").toLowerCase();

    if (lower.includes("hello") || lower.includes("hi") || lower.includes("hey")) {
        return `Hello! How can I assist you today? You can ask me about our luxury fleet, pricing, booking status, or driver dispatch across Andhra Pradesh hubs.`;
    }
    if (lower.includes("price") || lower.includes("rate") || lower.includes("cost") || lower.includes("rent")) {
        return `Our daily fleet rates start at ₹4,500/day for executive sedans up to ₹18,000/day for luxury sports cars like our Porsche 718 and Ferrari SF90. Full insurance and GPS telemetry are included.`;
    }
    if (lower.includes("car") || lower.includes("vehicle") || lower.includes("fleet") || lower.includes("model")) {
        return `Our premium fleet includes the Porsche 718 Cayman, Ferrari SF90 Spider, Chevrolet Corvette ZR1, Audi R8 V10, and Huracán EVO. All vehicles undergo comprehensive multi-point safety inspections.`;
    }
    if (lower.includes("driver") || lower.includes("deliver") || lower.includes("dispatch") || lower.includes("track")) {
        return `All dispatches are tracked live via our Google Maps Telemetry Hub. Your assigned fleet driver will contact you via WhatsApp and phone prior to delivery at your designated location.`;
    }
    if (lower.includes("location") || lower.includes("hub") || lower.includes("where") || lower.includes("pickup") || lower.includes("srm") || lower.includes("vijayawada") || lower.includes("guntur")) {
        return `We operate primary delivery hubs across Andhra Pradesh: SRM University-AP (Amaravati), Vijayawada Junction (BZA Hub), Mangalagiri AIIMS, and Guntur Central Terminal. Doorstep delivery is available.`;
    }
    if (lower.includes("book") || lower.includes("reserve") || lower.includes("order")) {
        return `To reserve a vehicle, browse our Vehicles page, select your desired dates and delivery hub, and confirm your booking. Our dispatch team will immediately assign a driver.`;
    }
    if (lower.includes("cancel") || lower.includes("refund")) {
        return `Cancellations made 24 hours prior to scheduled delivery qualify for a 100% full refund credited back to your original payment method within 2-3 business days.`;
    }
    if (lower.includes("contact") || lower.includes("phone") || lower.includes("number") || lower.includes("call")) {
        return `You can reach our 24/7 Operations Desk at support@fleetms.com or call our dispatch helpline via the Driver & Workplace portal.`;
    }
    if (lower.includes("thank") || lower.includes("ok") || lower.includes("great")) {
        return `You're very welcome! Please feel free to reach out anytime. Have a wonderful and safe journey with Fleet Management!`;
    }

    return `Thank you for reaching out! Our dispatch team has noted your query: "${userText}". An executive will assist you shortly, or you can check our Vehicles catalog and live Driver Workplace.`;
}

function getResponse() {
    return new Promise(function (resolve) {
        let messageList = getMessageList();
        let lastUserMessage = messageList.length > 0 ? messageList[messageList.length - 1] : "";
        setTimeout(function () {
            resolve({ "response": generateFleetSupportResponse(lastUserMessage) });
        }, 350);
    });
}

function getMessageList() {
    let res = [];
    let messageList = document.getElementsByClassName("message");
    for (let i = 0; i < messageList.length; i++) {
        res.push(messageList[i].innerHTML);
    }
    return res;
}

function heightToTop(ele) {
    //ele为指定跳转到该位置的DOM节点
    let root = document.body;
    let height = 0;
    do {
        height += ele.offsetTop;
        ele = ele.offsetParent;
    } while (ele !== root)
    return height;
}

function scrollToBottom() {
    let bottom = document.getElementById("char-area-bottom");
    let chatArea = document.getElementById("chat-area");
    chatArea.scrollTo({
        top: heightToTop(bottom),
        behavior: 'smooth'
    })
}

$inputBox.focus(function () {
    let container = document.getElementById("inputBox-comtainer");
    // add css class
    container.classList.add("comtainer-active");
}).blur(function () {
    let container = document.getElementById("inputBox-comtainer");
    container.classList.remove("comtainer-active");
});

function netWorkError() {
    let message = "Network error, please try again later";
    let messageRes = `
        <div class="error-message-container"><p class="error-message">${message}</p></div>
    `
    $("#char-area-bottom").before(messageRes);
}

function cleanResponse(response) {
    //let charater = [",", "!", "?", ".", ":", ";"];
    if (charater.includes(response[0])) response = response.slice(1);

    //let nowords = [".Human: ", "Human:"]
    for (let i = 0; i < nowords.length; i++) {
        response = response.replace(nowords[i], "");
    }
    return response;

}