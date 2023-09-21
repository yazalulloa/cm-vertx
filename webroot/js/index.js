import {
  Carousel,
  Datepicker,
  initTE,
  Input,
  Select,
  Timepicker
} from "tw-elements";
import './loader.js';
import './sse.js';

initTE({Carousel, Datepicker, Select, Timepicker, Input}, true); // set second parameter to true if you want to use a debugger

htmx.config.useTemplateFragments = true;

window.onload = function () {
  const inputs = document.getElementsByTagName('input');
  // console.log("inputs: {}", inputs.length);
  for (let i = 0; i < inputs.length; i++) {
    // console.log("input type: {}", inputs[i].type);
    if (inputs[i].type === 'search') {
      // console.log("setting trim")
      inputs[i].onchange = function () {
        let value = this.value;
        // console.log("trimming {}", value);
        this.value = this.value.replace(/^\s+/, '').replace(/\s+$/, '').trim();
        let val2 = this.value;
        // console.log("trimmed {}", val2);
      };
    }
  }
}

function trimInput(el) {
  // console.log("trimming {}", el.value)
  el.value = el.value.trim();
  // console.log("trimmed {} | ", el.value)
}

function getCookie(name) {
  const cookies = document.cookie.split('; ');
  for (let i = 0; i < cookies.length; i++) {
    const cookie = cookies[i].split('=')
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

const datepickerTranslated = new Datepicker(
    document.querySelector("#datepicker-translated"),
    {
      title: "Seleccione una fecha",
      monthsFull: [
        "Enero",
        "Febrero",
        "Marzo",
        "Abril",
        "Mayo",
        "Junio",
        "Julio",
        "Agosto",
        "Septiembre",
        "Octubre",
        "Noviembre",
        "Diciembre",
      ],
      monthsShort: [
        "Ene",
        "Feb",
        "Mar",
        "Abr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec",
      ],
      weekdaysFull: [
        "Domingo",
        "Lunes",
        "Martes",
        "Miércoles",
        "Jueves",
        "Viernes",
        "Sábado",
      ],
      weekdaysShort: ["Dom", "Lun", "Mar", "Mie", "Jue", "Vie", "Sab"],
      weekdaysNarrow: ["D", "L", "M", "M", "J", "V", "S"],
      okBtnText: "Ok",
      clearBtnText: "Borrar",
      cancelBtnText: "Cancelar",
    }
);