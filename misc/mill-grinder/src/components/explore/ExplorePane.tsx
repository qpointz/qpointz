import { FiCompass } from "react-icons/fi";

export default function ExplorePane() {
  return (
    <section className="flex-1 flex flex-col items-center justify-center">
      <div className="flex flex-col items-center">
        <FiCompass size={40} className="text-blue-600 mb-4" />
        <div className="text-2xl font-bold mb-2">Explore</div>
        <div className="text-gray-500 mb-6 max-w-lg text-center">
          This is the Explore tool. Add your custom exploration UI or integrations here!
        </div>
      </div>
    </section>
  );
}