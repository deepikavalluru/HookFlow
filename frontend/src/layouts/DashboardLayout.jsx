import Sidebar from "../components/dashboard/Sidebar.jsx";
import Header from "../components/dashboard/Header.jsx";

function DashboardLayout({ children, title }) {

    return (

        <div className="min-h-screen bg-slate-950 flex">

            <Sidebar />

            <div className="flex-1 min-w-0">

                <Header title={title} />

                <main className="p-4 sm:p-6 lg:p-8">

                    {children}

                </main>

            </div>

        </div>

    );

}

export default DashboardLayout;