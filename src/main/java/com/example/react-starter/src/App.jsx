import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import AdminLayout from './layouts/AdminLayout'
import AdminDashboard from './pages/admin/dashboard/AdminDashboard'
import Categories from './pages/admin/categories/Categories'

function Placeholder({ title }) {
  return (
      <div className="card border-0 shadow-sm">
        <div className="card-body p-5">
          <h2 className="fw-bold">{title}</h2>
          <p className="text-muted mb-0">
            Module en cours de développement.
          </p>
        </div>
      </div>
  )
}

function App() {
  return (
      <BrowserRouter>
        <Routes>
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<AdminDashboard />} />

            <Route
                path="ouvrages"
                element={<Placeholder title="Gestion des ouvrages" />}
            />

            <Route
                path="memoires"
                element={<Placeholder title="Gestion des mémoires" />}
            />

            <Route
                path="etudiants"
                element={<Placeholder title="Gestion des étudiants" />}
            />

            <Route
                path="filieres"
                element={<Placeholder title="Gestion des filières" />}
            />

            <Route
                path="niveaux"
                element={<Placeholder title="Gestion des niveaux" />}
            />

            <Route path="categories" element={<Categories />} />
          </Route>

          <Route
              path="*"
              element={<Navigate to="/admin" replace />}
          />
        </Routes>
      </BrowserRouter>
  )
}

export default App