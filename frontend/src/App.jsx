import RequireAdmin from './components/RequireAdmin';
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import Layout from './components/Layout'
import HomePage from './pages/HomePage'
import RegisterPage from './pages/RegisterPage'
import LoginPage from './pages/LoginPage'
import ElectionsPage from './pages/ElectionsPage'
import ElectionBallotPage from './pages/ElectionBallotPage'
import ResultsPage from './pages/ResultsPage'
import ChainExplorerPage from './pages/ChainExplorerPage'
import AdminElectionsPage from './pages/AdminElectionsPage'
import CreateElectionPage from './pages/CreateElectionPage'
import ManageElectionPage from './pages/ManageElectionPage'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Layout>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/elections" element={<ElectionsPage />} />
            <Route path="/elections/:electionId" element={<ElectionBallotPage />} />
            <Route path="/admin/elections/:electionId/results" element={<RequireAdmin><ResultsPage /></RequireAdmin>} />
            <Route path="/elections/:electionId/chain" element={<ChainExplorerPage />} />
            <Route path="/admin/elections" element={<RequireAdmin><AdminElectionsPage /></RequireAdmin>} />
            <Route path="/admin/elections/new" element={<RequireAdmin><CreateElectionPage /></RequireAdmin>} />
            <Route path="/admin/elections/:electionId" element={<RequireAdmin><ManageElectionPage /></RequireAdmin>} />
          </Routes>
        </Layout>
      </AuthProvider>
    </BrowserRouter>
  )
}