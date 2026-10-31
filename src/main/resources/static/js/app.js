
const API_URL = '/api/estabelecimentos';
let chartInstance = null;

document.addEventListener('DOMContentLoaded', carregarDados);

// CARREGAR DADOS
async function carregarDados(manterFiltros = false) {
    if (!manterFiltros) {
        document.getElementById('filtroUf').value = '';
        document.getElementById('filtroTipo').value = '';
    }

    const uf = document.getElementById('filtroUf').value;
    const tipo = document.getElementById('filtroTipo').value;

    const params = new URLSearchParams();

    if (uf) params.append('uf', uf);
    if (tipo) params.append('tipo', tipo);

    const url = manterFiltros && (uf || tipo)
        ? `${API_URL}/filtro?${params.toString()}`
        : API_URL;

    try {
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error('Erro ao carregar os dados.');
        }

        const data = await response.json();

        renderizarTabela(data);
        atualizarGrafico(data);

    } catch (error) {
        console.error("Erro ao buscar dados:", error);
    }
}

// APLICAR FILTRO
async function aplicarFiltro() {
    const uf = document.getElementById('filtroUf').value;
    const tipo = document.getElementById('filtroTipo').value;

    const params = new URLSearchParams();

    if (uf) params.append('uf', uf);
    if (tipo) params.append('tipo', tipo);

    try {
        const response = await fetch(`${API_URL}/filtro?${params.toString()}`);

        if (!response.ok) {
            throw new Error('Erro ao aplicar filtro.');
        }

        const data = await response.json();

        renderizarTabela(data);
        atualizarGrafico(data);

    } catch (error) {
        console.error("Erro no filtro:", error);
    }
}

// RENDERIZAR TABELA
function renderizarTabela(dados) {
    const tbody = document.getElementById('tabelaCorpo');

    tbody.innerHTML = '';

    dados.forEach(est => {
        const tr = document.createElement('tr');

        tr.innerHTML = `
            <td>${est.id}</td>
            <td>${est.codigoCnes || ''}</td>
            <td>${est.nomeEstabelecimento || ''}</td>
            <td>${est.uf || ''}</td>
            <td>${est.tipoEstabelecimento || ''}</td>
            <td>${est.atendeSus ? 'Sim' : 'Não'}</td>
            <td>
                <button class="btn btn-sm btn-warning me-1"
                    onclick="editar(${est.id})">
                    Editar
                </button>

                <button class="btn btn-sm btn-danger"
                    onclick="excluir(${est.id})">
                    Excluir
                </button>
            </td>
        `;

        tbody.appendChild(tr);
    });

    document.getElementById('contadorRegistros').textContent =
        `${dados.length} registros`;
}

// BUSCAR POR ID
async function buscarPorId() {
    const id = document.getElementById('buscaId').value;

    if (!id) {
        alert('Informe um ID para buscar.');
        return;
    }

    try {
        const response = await fetch(`${API_URL}/${id}`);

        if (!response.ok) {
            alert('Estabelecimento não encontrado.');
            return;
        }

        const data = await response.json();

        renderizarTabela([data]);
        atualizarGrafico([data]);

    } catch (error) {
        console.error("Erro ao buscar por ID:", error);
    }
}

// EDITAR ESTABELECIMENTO
async function editar(id) {
    try {
        const response = await fetch(`${API_URL}/${id}`);

        if (!response.ok) {
            alert('Erro ao buscar estabelecimento.');
            return;
        }

        const est = await response.json();

        document.getElementById('id').value = est.id;
        document.getElementById('codigoCnes').value = est.codigoCnes;
        document.getElementById('nomeEstabelecimento').value = est.nomeEstabelecimento;
        document.getElementById('uf').value = est.uf;
        document.getElementById('municipio').value = est.municipio;
        document.getElementById('tipoEstabelecimento').value = est.tipoEstabelecimento || '';
        document.getElementById('esferaAdministrativa').value = est.esferaAdministrativa || '';
        document.getElementById('atendeSus').checked = est.atendeSus;

        document.getElementById('formTitle').textContent = 'Editar Estabelecimento';

        document.getElementById('formMsg').innerHTML = '';

        document.getElementById('formEstabelecimento').scrollIntoView({
            behavior: 'smooth'
        });

    } catch (error) {
        console.error("Erro ao editar:", error);
    }
}

// SALVAR CADASTRO OU EDIÇÃO
document.getElementById('formEstabelecimento').addEventListener('submit', async function(event) {
    event.preventDefault();

    const id = document.getElementById('id').value;

    const estabelecimento = {
        codigoCnes: document.getElementById('codigoCnes').value,
        nomeEstabelecimento: document.getElementById('nomeEstabelecimento').value,
        uf: document.getElementById('uf').value,
        municipio: document.getElementById('municipio').value,
        tipoEstabelecimento: document.getElementById('tipoEstabelecimento').value,
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
            body: JSON.stringify(estabelecimento)
        });

        if (!response.ok) {
            throw new Error('Erro ao salvar estabelecimento.');
        }

        document.getElementById('formMsg').innerHTML =
            '<span class="text-success">Estabelecimento salvo com sucesso!</span>';

        limparFormulario();

        await carregarDados(true);

    } catch (error) {
        document.getElementById('formMsg').innerHTML =
            '<span class="text-danger">Erro ao salvar estabelecimento.</span>';

        console.error("Erro ao salvar:", error);
    }
});

// LIMPAR FORMULÁRIO / CANCELAR
function limparFormulario() {
    document.getElementById('formEstabelecimento').reset();

    document.getElementById('id').value = '';

    document.getElementById('formTitle').textContent =
        'Cadastrar Estabelecimento';

    document.getElementById('formMsg').innerHTML = '';
}

// EXCLUIR ESTABELECIMENTO
async function excluir(id) {
    if (confirm('Confirmar exclusão?')) {
        try {
            const response = await fetch(`${API_URL}/${id}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error('Erro ao excluir estabelecimento.');
            }

            await carregarDados(true);

        } catch (error) {
            console.error("Erro ao excluir:", error);
            alert('Não foi possível excluir o estabelecimento.');
        }
    }
}

// IMPORTAR CSV
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

// ATUALIZAR GRÁFICO
function atualizarGrafico(dados) {
    const ctx = document.getElementById('ufChart').getContext('2d');

    const contagemUf = {};

    dados.forEach(d => {
        contagemUf[d.uf] = (contagemUf[d.uf] || 0) + 1;
    });

    if (chartInstance) {
        chartInstance.destroy();
    }

    chartInstance = new Chart(ctx, {
        type: 'pie',

        data: {
            labels: Object.keys(contagemUf),

            datasets: [{
                label: 'Estabelecimentos',

                data: Object.values(contagemUf),

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
