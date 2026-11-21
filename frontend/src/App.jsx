import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { lazy, Suspense } from "react";

import ProtectedRoute from "./components/ProtectedRoute";
import { AuthProvider } from "./context/AuthContext";

const Login = lazy(() => import("./pages/Login"));
const Register = lazy(() => import("./pages/Register"));
const Dashboard = lazy(() => import("./pages/Dashboard"));

const Webhooks = lazy(() => import("./pages/Webhooks"));
const WebhookDetails = lazy(() => import("./pages/WebhookDetails"));

const Events = lazy(() => import("./pages/Events"));
const EventDetails = lazy(() => import("./pages/EventDetails"));

const Deliveries = lazy(() => import("./pages/Deliveries"));
const DeliveryDetails = lazy(() => import("./pages/DeliveryDetails"));

const Settings = lazy(() => import("./pages/Settings"));

function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <Suspense
                    fallback={
                        <div className="min-h-screen bg-gray-950 flex items-center justify-center">
                            <div className="text-gray-300 text-lg">
                                Loading HookFlow...
                            </div>
                        </div>
                    }
                >
                    <Routes>
                        <Route path="/login" element={<Login />} />
                        <Route path="/register" element={<Register />} />

                        <Route element={<ProtectedRoute />}>
                            <Route path="/dashboard" element={<Dashboard />} />

                            <Route path="/webhooks" element={<Webhooks />} />
                            <Route
                                path="/webhooks/:id"
                                element={<WebhookDetails />}
                            />

                            <Route path="/events" element={<Events />} />
                            <Route
                                path="/events/:id"
                                element={<EventDetails />}
                            />

                            <Route path="/deliveries" element={<Deliveries />} />
                            <Route
                                path="/deliveries/:id"
                                element={<DeliveryDetails />}
                            />

                            {/* Settings */}
                            <Route path="/settings" element={<Settings />} />
                        </Route>

                        <Route
                            path="*"
                            element={<Navigate to="/dashboard" replace />}
                        />
                    </Routes>
                </Suspense>
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;