import axios from 'axios'

const API_URL = 'http://localhost:8080/api/categories'

const categoryApi = {
    getAll: async () => {
        const response = await axios.get(API_URL)
        return response.data
    },

    create: async (nom) => {
        const response = await axios.post(API_URL, { nom })
        return response.data
    },

    update: async (id, nom) => {
        const response = await axios.put(`${API_URL}/${id}`, { nom })
        return response.data
    },

    delete: async (id) => {
        await axios.delete(`${API_URL}/${id}`)
    },
}

export default categoryApi