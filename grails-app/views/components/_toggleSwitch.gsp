<%-- 
  Composant pour afficher un toggle switch similaire à celui du thème
  
  Paramètres:
  - name: nom du champ (obligatoire)
  - checked: état initial du toggle (obligatoire)
  - label: libellé à afficher (obligatoire)
--%>
<div class="field">
    <div class="control theme-switch-wrapper">
        <label class="label mr-2" for="${name}">${label}</label>
        <label class="theme-switch" for="${name}">
            <input type="checkbox" id="${name}" name="${name}" ${checked ? 'checked' : ''}/>
            <div class="slider"></div>
        </label>
    </div>
</div>

<g:javascript>
  document.addEventListener('DOMContentLoaded', function() {
    const toggle = document.getElementById('${name}');
    if (toggle) {
      toggle.addEventListener('change', function() {
        const statusSpan = this.closest('.theme-switch-wrapper').querySelector('.toggle-status');
        if (statusSpan) {
          statusSpan.textContent = this.checked ? 'Activé' : 'Désactivé';
        }
      });
    }
  });
</g:javascript>
