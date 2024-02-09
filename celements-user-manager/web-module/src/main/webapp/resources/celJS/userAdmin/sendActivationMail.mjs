/* Request über Fetch API absetzen */
async function sendActivationMail(url) {
  try {
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error('response status: ' + response.status + ' ' + response.statusText + ' for url: ' + url);
      }
      const data = await response.json();
      alert(data.message);
  } catch (error) { 
    console.error('Error during fetch operation:', error);
  }
}


/* normale Aktion des a-Tags unterdrücken */
function stopDefaultAction(event) {
    event.preventDefault();
    sendActivationMail(event.currentTarget.href);
}

/* EventListener für a Tags registrieren */
document.querySelectorAll("a.sendMailAction").forEach(a => {
    a.addEventListener('click', stopDefaultAction);
});


