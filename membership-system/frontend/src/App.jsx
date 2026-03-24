import React from 'react'
import { BrowserRouter as Router, Switch, Route, Redirect } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import Login from './pages/Login'
import CustomLayout from './components/Layout'
import Dashboard from './pages/Dashboard'
import UserManagement from './pages/UserManagement'
import RoleManagement from './pages/RoleManagement'
import PermissionManagement from './pages/PermissionManagement'
import SupplierManagement from './pages/SupplierManagement'
import ProductManagement from './pages/ProductManagement'
import CustomerManagement from './pages/CustomerManagement'
import CustomerProductManagement from './pages/CustomerProductManagement'
import CustomerPaymentManagement from './pages/CustomerPaymentManagement'
import CustomerBalanceManagement from './pages/CustomerBalanceManagement'
import RechargeRecordManagement from './pages/RechargeRecordManagement'
import SupplierProductManagement from './pages/SupplierProductManagement'
import SupplierBalanceManagement from './pages/SupplierBalanceManagement'
import PrivateRoute from './components/PrivateRoute'

function App() {
  return (
    <AuthProvider>
      <Router>
        <Switch>
          <Route path="/login" component={Login} />
          <PrivateRoute path="/">
            <CustomLayout>
              <Switch>
                <Route exact path="/" component={Dashboard} />
                <Route path="/users" component={UserManagement} />
                <Route path="/roles" component={RoleManagement} />
                <Route path="/permissions" component={PermissionManagement} />
                <Route path="/suppliers" component={SupplierManagement} />
                <Route path="/supplier-products" component={SupplierProductManagement} />
                <Route path="/supplier-balances" component={SupplierBalanceManagement} />
                <Route path="/products" component={ProductManagement} />
                <Route path="/customers" component={CustomerManagement} />
                <Route path="/customer-products" component={CustomerProductManagement} />
                <Route path="/customer-payments" component={CustomerPaymentManagement} />
                <Route path="/customer-balances" component={CustomerBalanceManagement} />
                <Route path="/recharge-records" component={RechargeRecordManagement} />
                <Redirect to="/" />
              </Switch>
            </CustomLayout>
          </PrivateRoute>
          <Redirect to="/" />
        </Switch>
      </Router>
    </AuthProvider>
  )
}

export default App