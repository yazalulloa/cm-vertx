htmx.config.useTemplateFragments = true;

window.onload = function() {
  const inputs = document.getElementsByTagName('input');
  console.log("inputs: {}", inputs.length);
  for (let i = 0; i < inputs.length; i++) {
    console.log("input type: {}", inputs[i].type);
    if (inputs[i].type === 'search') {
      console.log("setting trim")
      inputs[i].onchange = function() {
        let value = this.value;
        console.log("trimming {}", value);
        this.value = this.value.replace(/^\s+/, '').replace(/\s+$/, '').trim();
        let val2 = this.value;
        console.log("trimmed {}", val2);
      };
    }
  }
}

function trimInput(el) {
  console.log("trimming {}", el.value)
  el.value = el.value.trim();
  console.log("trimmed {} | ", el.value)
}

function getCookie(name) {
  const cookies = document.cookie.split('; ');
  for (let i = 0; i < cookies.length; i++) {
    const cookie = cookies[i].split('=');
    if (cookie[0] === name) {
      return decodeURIComponent(cookie[1]);
    }
  }
  return null;
}

document.body.addEventListener("htmx:configRequest", function (configEvent) {

  let token = getCookie('XSRF-TOKEN');
  if (token != null) {
    configEvent.detail.headers['X-XSRF-TOKEN'] = token;
  }

})

/* Set the width of the side navigation to 250px and the left margin of the page content to 250px */
function openNav() {
  document.getElementById("mySidenav").style.width = "250px";
  document.getElementById("main").style.marginLeft = "250px";
}

/* Set the width of the side navigation to 0 and the left margin of the page content to 0 */
function closeNav() {
  document.getElementById("mySidenav").style.width = "0";
  document.getElementById("main").style.marginLeft = "0";
}