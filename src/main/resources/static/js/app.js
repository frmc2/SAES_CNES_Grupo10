const API_URL = 'http://localhost:8080/api/Unidades';
let chartInstance = null;

document.addEventListener('DOMContentLoaded', carregarDados);

async function carregarDados(manterFiltros = false) {
    if (!manterFiltros) {
        document.getElementById('filtroUf').value = '';
        document.getElementById('filtroTipo').value = '';
    }

    const UF = document.getElementById('filtroUf').value;
    const tipo = document.getElementById('filtroTipo').value;
    const params = new URLSearchParams();

    if (UF) params.append('UF', UF);
    if (tipo) params.append('tipoEstabelecimento', tipo);

    const url = manterFiltros && (UF || tipo) ? `${API_URL}/filtro?${params.toString()}` : API_URL;

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

async function aplicarFiltro() {
    const UF = document.getElementById('filtroUf').value;
    const tipo = document.getElementById('filtroTipo').value;
    const params = new URLSearchParams();

    if (UF) params.append('UF', UF);
    if (tipo) params.append('tipoEstabelecimento', tipo);

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

function renderizarTabela(unidades) {
    const tbody = document.getElementById('tabelaCorpo');

    tbody.innerHTML = '';

    unidades.forEach(unidade => {
        const tr = document.createElement('tr');

            tr.innerHTML = `
            <td>${unidade.id}</td>
            <td>${unidade.codigoCnes || ''}</td>
            <td>${unidade.nome || ''}</td>
            <td>${unidade.uf || unidade.UF || ''}</td>
            <td>${unidade.municipio || ''}</td>
            <td>${unidade.tipoEstabelecimento || ''}</td>
            <td>${unidade.esferaAdministrativa || ''}</td>
            <td>${unidade.latitude ?? ''}</td>
            <td>${unidade.longitude ?? ''}</td>
            <td>
                <button class="btn btn-sm btn-warning me-1" onclick="editar(${unidade.id})">Editar</button>
                <button class="btn btn-sm btn-danger" onclick="excluir(${unidade.id})">Excluir</button>
            </td>`;

        tbody.appendChild(tr);
    });

    document.getElementById('contadorRegistros').textContent = `${unidades.length} registros`;
}

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
        document.getElementById('uf').value = unidade.uf || unidade.UF;
        document.getElementById('municipio').value = unidade.municipio;
        document.getElementById('tipo').value = unidade.tipoEstabelecimento || '';
        document.getElementById('esferaAdministrativa').value = unidade.esferaAdministrativa || '';

        document.getElementById('formTitle').textContent = 'Editar Unidade';

        document.getElementById('formMsg').innerHTML = '';

        document.getElementById('formUnidades').scrollIntoView({
            behavior: 'smooth'
        });

    } catch (error) {
        console.error("Erro ao editar unidade:", error);
    }
}

document.getElementById('formUnidades').addEventListener('submit', async function(event) {
    event.preventDefault();

    const id = document.getElementById('id').value;

    const unidade = {
        codigoCnes: document.getElementById('codigoCnes').value,
        nome: document.getElementById('nome').value,
        UF: document.getElementById('uf').value,
        municipio: document.getElementById('municipio').value,
        tipoEstabelecimento: document.getElementById('tipo').value,
        esferaAdministrativa: document.getElementById('esferaAdministrativa').value
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
            const erro = await response.text();
            console.error('Erro retornado pelo backend:', {
                status: response.status,
                mensagem: erro
            });
            document.getElementById('formMsg').innerHTML = `<span class="text-danger">Erro HTTP: ${response.status}</span>`;
            return;
        }

        document.getElementById('formMsg').innerHTML = '<span class="text-success">Unidade salva com sucesso!</span>';

        limparFormulario();

        await carregarDados(true);
    } catch (error) {
        document.getElementById('formMsg').innerHTML = '<span class="text-danger">Erro ao salvar unidade.</span>';
        console.error("Erro ao salvar unidade:", error);
    }
});

function limparFormulario() {
    document.getElementById('formUnidades').reset();
    document.getElementById('id').value = '';
    document.getElementById('formTitle').textContent = 'Cadastrar Unidade';
    document.getElementById('formMsg').innerHTML = '';
}

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

async function importarDados() {
    const fileInput = document.getElementById('fileInput');
    const msgDiv = document.getElementById('importMsg');

    if (!fileInput.files[0]) {
        msgDiv.innerHTML =
            '<span class="text-danger">Selecione um arquivo.</span>';
        return;
    }

    const formData = new FormData();
    formData.append('arquivo', fileInput.files[0]);

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
            msgDiv.innerHTML = '<span class="text-danger">Erro estrutural no arquivo.</span>';
        }

    } catch (error) {
        msgDiv.innerHTML = '<span class="text-danger">Erro de rede.</span>';
        console.error("Erro na importação:", error);
    }
}

function atualizarGrafico(unidades) {
    const ctx = document.getElementById('ufChart').getContext('2d');
    const contagemUF = {};

    unidades.forEach(unidade => {
        const uf = unidade.uf || unidade.UF || 'N/A';
        contagemUF[uf] = (contagemUF[uf] || 0) + 1;
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
                backgroundColor: ['#0d6efd', '#6c757d', '#198754', '#dc3545', '#ffc107', '#0dcaf0']
            }]
        }
    });
}
