(function () {
  var pageState = {};

  function getRows(table) {
    return Array.prototype.slice.call(
      table.querySelectorAll("tbody tr[data-record='true']")
    );
  }

  function renderTable(table) {
    var rows = getRows(table);
    var search = document.getElementById(table.dataset.searchInput);
    var status = document.getElementById(table.dataset.statusFilter);
    var linkage = document.getElementById(table.dataset.linkageFilter);
    var query = search ? search.value.trim().toLocaleLowerCase() : "";
    var selectedStatus = status ? status.value : "";
    var selectedLinkage = linkage ? linkage.value : "";
    var matches = rows.filter(function (row) {
      var searchable = row.textContent.toLocaleLowerCase();
      return searchable.indexOf(query) !== -1
        && (!selectedStatus || row.dataset.status === selectedStatus)
        && (!selectedLinkage || row.dataset.linkage === selectedLinkage);
    });

    var pageSize = 10;
    var maxPage = Math.max(1, Math.ceil(matches.length / pageSize));
    var state = pageState[table.id] || { page: 0 };
    state.page = Math.min(state.page, maxPage - 1);
    pageState[table.id] = state;
    var start = state.page * pageSize;
    var end = Math.min(start + pageSize, matches.length);
    var visible = matches.slice(start, end);

    rows.forEach(function (row) {
      row.hidden = visible.indexOf(row) === -1;
    });

    var emptyRow = table.querySelector("tbody tr[data-empty='true']");
    if (emptyRow) emptyRow.hidden = matches.length > 0;

    var count = document.getElementById(table.dataset.countLabel);
    if (count) {
      count.textContent = matches.length === 0
        ? "Sin resultados"
        : "Mostrando " + (start + 1) + "-" + end + " de " + matches.length;
    }

    var page = document.getElementById(table.dataset.pageLabel);
    if (page) {
      page.textContent = (state.page + 1) + " / " + maxPage;
    }

    var previous = document.getElementById(table.dataset.previousButton);
    var next = document.getElementById(table.dataset.nextButton);
    if (previous) previous.disabled = state.page === 0;
    if (next) next.disabled = state.page >= maxPage - 1;
  }

  window.filterPeopleTable = function (tableId) {
    var table = document.getElementById(tableId);
    if (!table) return;
    pageState[tableId] = { page: 0 };
    renderTable(table);
  };

  window.changePeoplePage = function (tableId, offset) {
    var table = document.getElementById(tableId);
    if (!table) return;
    var state = pageState[tableId] || { page: 0 };
    state.page += offset;
    pageState[tableId] = state;
    renderTable(table);
  };

  window.togglePeopleForm = function (formId) {
    var form = document.getElementById(formId);
    if (!form) return;
    if (form.hasAttribute("hidden")) {
      form.hidden = !form.hidden;
    } else {
      form.style.display = form.style.display === "none" ? "block" : "none";
    }
  };

  window.togglePasswordVisibility = function (inputId, button) {
    var input = document.getElementById(inputId);
    if (!input) return;
    var visible = input.type === "password";
    input.type = visible ? "text" : "password";
    button.setAttribute("aria-pressed", visible ? "true" : "false");
    button.setAttribute("aria-label", visible ? "Ocultar contraseña" : "Mostrar contraseña");
    button.title = visible ? "Ocultar contraseña" : "Mostrar contraseña";
    button.textContent = visible ? "Ocultar" : "Mostrar";
  };

  window.showTeacherMode = function (mode) {
    document.querySelectorAll("[data-teacher-view]").forEach(function (view) {
      view.hidden = view.dataset.teacherView !== mode;
    });
    document.querySelectorAll("[data-teacher-mode]").forEach(function (tab) {
      tab.classList.toggle("active", tab.dataset.teacherMode === mode);
    });
  };

  document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".people-table[data-search-input]").forEach(renderTable);
  });
}());
