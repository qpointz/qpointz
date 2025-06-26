export default function OverviewServices() {
  const services = [
    {
      name: "gRPC Service",
      endpoint: "grpc://prod-data-service:50051",
      parameters: ["userId", "dateRange"],
      description: "High-performance gRPC API for real-time data queries."
    },
    {
      name: "HTTP REST API",
      endpoint: "https://api.prod-data.com/v1",
      parameters: ["apiKey", "query"],
      description: "Standard HTTP interface with JSON responses."
    },
    {
      name: "Bulk Data Service",
      endpoint: "s3://prod-data-bucket",
      parameters: ["date", "format"],
      description: "Bulk exports of data in Parquet and CSV formats."
    }
  ];

  return (
    <div>
      <h1 className="text-3xl font-bold mb-6 text-gray-800">Services</h1>
      <div className="space-y-6">
        {services.map((service) => (
          <div key={service.name} className="bg-white rounded shadow p-6">
            <h2 className="text-xl font-semibold mb-1">{service.name}</h2>
            <p className="mb-2 text-gray-700">{service.description}</p>
            <div><strong>Endpoint:</strong> {service.endpoint}</div>
            <div><strong>Parameters:</strong> {service.parameters.join(", ")}</div>
          </div>
        ))}
      </div>
    </div>
  );
}