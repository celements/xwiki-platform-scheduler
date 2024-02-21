class SendMailButton {
  #htmlElemA;
  #iconElem;
  constructor(htmlElemA) {
    this.#htmlElemA = htmlElemA;
    this.#iconElem = this.#htmlElemA.querySelector(".icon");
  }

  register() {
    console.log('registering event listener for ' + this.#htmlElemA);
    this.#htmlElemA.addEventListener('click', (ev) => this.#stopDefaultAction(ev));
  }

  #stopDefaultAction(event) {
    console.log('stopDefaultAction');
    event.preventDefault();
    this.#iconElem.classList.add("fa-spinner", "fa-spin");
    this.#sendActivationMail(this.#htmlElemA.href);
  }

  async #sendActivationMail(url) {
    console.log('sendActivationMail with url: ' + url);
    try {
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error('response status: ' + response.status + ' ' + response.statusText + ' for url: ' + url);
      }
      const data = await response.json();
      console.log('successful: ' + data.successful + ' message: ' + data.message);
      this.#showOutput(data.message);
    } catch (error) { 
      console.error('Error during fetch operation:', error);
      this.#showOutput("Es ist ein Fehler aufgetreten. Bitte versuchen Sie es später erneut.\n" + error.message);
    } finally {
      this.#iconElem.classList.remove("fa-spinner", "fa-spin");
    }
  }

  #showOutput(message) {
    alert(message);
  }
}

document.querySelectorAll("a.sendMailAction").forEach((a) => new SendMailButton(a).register());




/*___________________________________________________________

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

function stopDefaultAction(event) {
    event.preventDefault();
    event.target.classList.add("fa-spinner", "fa-spin");
    sendActivationMail(event, event.currentTarget.href);
}

document.querySelectorAll("a.sendMailAction").forEach(a => {
    a.addEventListener('click', stopDefaultAction);
});
*/
