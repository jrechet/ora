<%-- _hotspot.gsp --%>
<div id="tab-hotspot" class="tab-content ${active ? 'is-active' : ''}">
    <div class="card">
        <div class="card-content px-5 py-4">
            <table class="table is-fullwidth monitoring-table">
                <thead>
                <tr>
                    <th class="has-text-grey is-size-6 py-3 pl-2" style="font-weight: 500; border-bottom: 1px solid #f0f0f0;">
                        <span>Environment</span>
                    </th>
                    <th class="has-text-grey is-size-6 py-3" style="font-weight: 500; border-bottom: 1px solid #f0f0f0;">
                        <span>Status</span>
                    </th>
                    <th class="has-text-grey is-size-6 py-3" style="font-weight: 500; border-bottom: 1px solid #f0f0f0;">
                        <span>Problems</span>
                    </th>
                </tr>
                </thead>
                <tbody class="has-text-weight-medium">
                <%-- Table body will be populated by JavaScript --%>
                </tbody>
            </table>
        </div>
    </div>

    <div class="card">
        <div class="card-content px-5 py-4">
            <table class="table is-fullwidth monitoring-table">
                <thead>
                <tr>
                    <th class="has-text-grey is-size-6 py-3 pl-2" style="font-weight: 500; border-bottom: 1px solid #f0f0f0;">
                        <span>CI/CD Issues</span>
                    </th>
                </tr>
                </thead>
                <tbody id="cicd-issues">
                <%-- CI/CD issues will be populated by JavaScript --%>
                </tbody>
            </table>
        </div>
    </div>
</div>

<style>
.monitoring-table {
    background: transparent;
}

.monitoring-table th {
    letter-spacing: 0.5px;
    text-transform: uppercase;
    background: transparent;
}

.monitoring-table td {
    padding: 1rem 0.5rem;
    border-bottom: 1px solid #f5f5f5;
    vertical-align: middle;
}

.monitoring-table tbody tr:last-child td {
    border-bottom: none;
}

.monitoring-table tbody tr:hover {
    background-color: #fafafa;
}
</style>