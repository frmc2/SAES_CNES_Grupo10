
const API_URL = '/api/Unidades';
let chartInstance = null;

document.addEventListener('DOMContentLoaded', carregarDados);

// CARREGAR UNIDADES
async function carregarDados(manterFiltros = false) {
    if (!manterFiltros) {
        document.getElementById('filtroUf').value = '';
        document.getElementById('filtroTipo').value = '';
    }

    const UF = document.getElementById('filtroUf').value;
    const tipo = document.getElementById('filtroTipo').value;

    const params = new URLSearchParams();

    if (UF) params.append('uf', UF);
    if (tipo) params.append('tipo', tipo);

    const url = manterFiltros && (UF || tipo)
        ? `${API_URL}/filtro?${params.toString()}`
        : API_URL;

    try {
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error('Erro ao carregar unidades.');
        }

        const unidades = await response.json();

        renderizarTabela(unidades);
        atualizarGrafico(unidades);

    } catch (error) {
        console.error("Erro ao buscar unidades:", error);
    }
}

// APLICAR FILTRO
async function aplicarFiltro() {
    const UF = document.getElementById('filtroUf').value;
    const tipo = document.getElementById('filtroTipo').value;

    const params = new URLSearchParams();

    if (UF) params.append('uf', UF);
    if (tipo) params.append('tipo', tipo);

    try {
        const response = await fetch(`${API_URL}/filtro?${params.toString()}`);

        if (!response.ok) {
            throw new Error('Erro ao aplicar filtro.');
        }

        const unidades = await response.json();

        renderizarTabela(unidades);
        atualizarGrafico(unidades);

    } catch (error) {
        console.error("Erro no filtro:", error);
    }
}

// RENDERIZAR TABELA DE UNIDADES
function renderizarTabela(unidades) {
    const tbody = document.getElementById('tabelaCorpo');

    tbody.innerHTML = '';

    unidades.forEach(unidade => {
        const tr = document.createElement('tr');

        tr.innerHTML = `
            <td>${unidade.id}</td>
            <td>${unidade.codigoCnes || ''}</td>
            <td>${unidade.nome || ''}</td>
            <td>${unidade.UF || ''}</td>
            <td>${unidade.tipo || ''}</td>
            <td>${unidade.atendeSus ? 'Sim' : 'Não'}</td>
            <td>
                <button class="btn btn-sm btn-warning me-1"
                    onclick="editar(${unidade.id})">
                    Editar
                </button>

                <button class="btn btn-sm btn-danger"
                    onclick="excluir(${unidade.id})">
                    Excluir
                </button>
            </td>
        `;

        tbody.appendChild(tr);
    });

    document.getElementById('contadorRegistros').textContent =
        `${unidades.length} registros`;
}

// BUSCAR UNIDADE POR ID
async function buscarPorId() {
    const id = document.getElementById('buscaId').value;

    if (!id) {
        alert('Informe um ID para buscar.');
        return;
    }

    try {
        const response = await fetch(`${API_URL}/${id}`);

        if (!response.ok) {
            alert('Unidade não encontrada.');
            return;
        }

        const unidade = await response.json();

        renderizarTabela([unidade]);
        atualizarGrafico([unidade]);

    } catch (error) {
        console.error("Erro ao buscar unidade:", error);
    }
}

// EDITAR UNIDADE
async function editar(id) {
    try {
        const response = await fetch(`${API_URL}/${id}`);

        if (!response.ok) {
            alert('Erro ao buscar unidade.');
            return;
        }

        const unidade = await response.json();

        document.getElementById('id').value = unidade.id;
        document.getElementById('codigoCnes').value = unidade.codigoCnes;
        document.getElementById('nome').value = unidade.nome;
        document.getElementById('uf').value = unidade.UF;
        document.getElementById('municipio').value = unidade.municipio;
        document.getElementById('tipo').value = unidade.tipo || '';
        document.getElementById('esferaAdministrativa').value = unidade.esferaAdministrativa || '';
        document.getElementById('atendeSus').checked = unidade.atendeSus;

        document.getElementById('formTitle').textContent =
            'Editar Unidade';

        document.getElementById('formMsg').innerHTML = '';

        document.getElementById('formUnidades').scrollIntoView({
            behavior: 'smooth'
        });

    } catch (error) {
        console.error("Erro ao editar unidade:", error);
    }
}

// SALVAR OU ATUALIZAR UNIDADE
document.getElementById('formUnidades').addEventListener('submit', async function(event) {
    event.preventDefault();

    const id = document.getElementById('id').value;

    const unidade = {
        codigoCnes: document.getElementById('codigoCnes').value,
        nome: document.getElementById('nome').value,
        UF: document.getElementById('uf').value,
        municipio: document.getElementById('municipio').value,
        tipoUnidades: document.getElementById('tipo').value,
        esferaAdministrativa: document.getElementById('esferaAdministrativa').value,
        atendeSus: document.getElementById('atendeSus').checked
    };

    const metodo = id ? 'PUT' : 'POST';
    const url = id ? `${API_URL}/${id}` : API_URL;

    try {
        const response = await fetch(url, {
            method: metodo,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(unidade)
        });

        if (!response.ok) {
            throw new Error('Erro ao salvar unidade.');
        }

        document.getElementById('formMsg').innerHTML =
            '<span class="text-success">Unidade salva com sucesso!</span>';

        limparFormulario();

        await carregarDados(true);

    } catch (error) {
        document.getElementById('formMsg').innerHTML =
            '<span class="text-danger">Erro ao salvar unidade.</span>';

        console.error("Erro ao salvar unidade:", error);
    }
});

// CANCELAR / LIMPAR FORMULÁRIO
function limparFormulario() {
    document.getElementById('formUnidades').reset();

    document.getElementById('id').value = '';

    document.getElementById('formTitle').textContent =
        'Cadastrar Unidade';

    document.getElementById('formMsg').innerHTML = '';
}

// EXCLUIR UNIDADE
async function excluir(id) {
    if (confirm('Confirmar exclusão desta unidade?')) {
        try {
            const response = await fetch(`${API_URL}/${id}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error('Erro ao excluir unidade.');
            }

            await carregarDados(true);

        } catch (error) {
            console.error("Erro ao excluir unidade:", error);
            alert('Não foi possível excluir a unidade.');
        }
    }
}

// IMPORTAR UNIDADES CSV
async function importarDados() {
    const fileInput = document.getElementById('fileInput');
    const msgDiv = document.getElementById('importMsg');

    if (!fileInput.files[0]) {
        msgDiv.innerHTML =
            '<span class="text-danger">Selecione um arquivo.</span>';
        return;
    }

    const formData = new FormData();

    formData.append('file', fileInput.files[0]);

    try {
        const response = await fetch(`${API_URL}/importar`, {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            const res = await response.json();

            msgDiv.innerHTML = `
                <span class="text-success">
                    Sucesso! Lidas: ${res.lidas},
                    Importadas: ${res.importadas},
                    Duplicadas: ${res.duplicadas},
                    Rejeitadas: ${res.rejeitadas}.
                </span>
            `;

            await carregarDados(true);

        } else {
            msgDiv.innerHTML =
                '<span class="text-danger">Erro estrutural no arquivo.</span>';
        }

    } catch (error) {
        msgDiv.innerHTML =
            '<span class="text-danger">Erro de rede.</span>';

        console.error("Erro na importação:", error);
    }
}

// ATUALIZAR GRÁFICO POR UF
function atualizarGrafico(unidades) {
    const ctx = document.getElementById('ufChart').getContext('2d');

    const contagemUF = {};

    unidades.forEach(unidade => {
        contagemUF[unidade.UF] = (contagemUF[unidade.UF] || 0) + 1;
    });

    if (chartInstance) {
        chartInstance.destroy();
    }

    chartInstance = new Chart(ctx, {
        type: 'pie',

        data: {
            labels: Object.keys(contagemUF),

            datasets: [{
                label: 'Unidades',

                data: Object.values(contagemUF),

                backgroundColor: [
                    '#0d6efd',
                    '#6c757d',
                    '#198754',
                    '#dc3545',
                    '#ffc107',
                    '#0dcaf0'
                ]
            }]
        }
    });
}
