/* Request über Fetch API absetzen */
async function sendActivationMail(event, url) {
  try {
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error('response status: ' + response.status + ' ' + response.statusText + ' for url: ' + url);
      }
      const data = await response.json();
      alert(data.message);
      event.target.classList.remove("fa-spinner", "fa-spin");
  } catch (error) { 
    console.error('Error during fetch operation:', error);
    alert("Es ist ein Fehler aufgetreten. Bitte versuchen Sie es später erneut." + '\nresponse status: ' + response.status + ' ' + response.statusText);
    event.target.classList.remove("fa-spinner", "fa-spin");
  }
}

/* normale Aktion des a-Tags unterdrücken */
function stopDefaultAction(event) {
    event.preventDefault();
    event.target.classList.add("fa-spinner", "fa-spin");
    sendActivationMail(event.currentTarget.href);
}

/* EventListener für a Tags registrieren */
document.querySelectorAll("a.sendMailAction").forEach(a => {
    a.addEventListener('click', stopDefaultAction);
});
